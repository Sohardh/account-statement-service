package com.sohardh.account.service.category;

import com.sohardh.account.model.FireflyStatement;
import com.sohardh.account.model.StatementModel;
import com.sohardh.account.repositories.FireflyStatementRepository;
import com.sohardh.account.repositories.StatementRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategorizationService {

  private final StatementRepository statementRepository;
  private final FireflyStatementRepository fireflyStatementRepository;

  public CategorizationService(StatementRepository statementRepository,
      FireflyStatementRepository fireflyStatementRepository) {
    this.statementRepository = statementRepository;
    this.fireflyStatementRepository = fireflyStatementRepository;
  }

  public void categorize() {
    var statements = statementRepository.findAllNotProcessed();
    log.info("Starting regex categorization of {} statements", statements.size());

    List<String> internalReferences = statements.stream()
        .map(StatementModel::getInternalReference)
        .toList();
    var existingInternalReferences = fireflyStatementRepository.getAllByInternalReferences(
        internalReferences).stream().map(FireflyStatement::getInternalReference).toList();

    var fireflyStatements = statements.stream()
        .filter(statementModel -> !existingInternalReferences.contains(
            statementModel.getInternalReference()))
        .map(this::analyzeTransactions)
        .toList();

    fireflyStatementRepository.saveAll(fireflyStatements);
    statementRepository.saveAll(statements);
    log.info("Regex categorization finished. Total categorized statements: {}",
        fireflyStatements.size());
  }

  private FireflyStatement analyzeTransactions(StatementModel statementModel) {
    var fireflyStatement = new FireflyStatement(statementModel);
    parseAndPopulateUpi(fireflyStatement);
    parseAndPopulateStocksDebit(fireflyStatement);
    parseAndPopulateStocksCredit(fireflyStatement);
    parseAndPopulateHdfc(fireflyStatement);
    parseAndPopulateSalary(fireflyStatement);
    parseAndPopulateMisc(fireflyStatement);
    statementModel.setIsProcessed(true);
    statementModel.setProcessedAt(LocalDate.now());
    if (StringUtils.isEmpty(fireflyStatement.getOpposingAccount())) {
      fireflyStatement.setOpposingAccount("Unknown");
    }
    return fireflyStatement;
  }

  private void parseAndPopulateStocksDebit(FireflyStatement fireflyStatement) {
    var description = fireflyStatement.getDescription();
    if (!description.startsWith("ACH D-")) {
      return;
    }
    fireflyStatement.addTag("stock");
    fireflyStatement.setCategory("stock");

    String[] split = description.split("-");
    if (split.length <= 1) {
      return;
    }

    if (description.contains("SBISMSMFB")) {
      fireflyStatement.setOpposingAccount("SBI Mutual Funds");
      fireflyStatement.addTag("mutual_funds");
    } else if (description.contains("INDIAN CLEARING CORP")) {
      fireflyStatement.setOpposingAccount("Groww Mutual Funds");
      fireflyStatement.addTag("mutual_funds");
    }

  }

  private void parseAndPopulateStocksCredit(FireflyStatement fireflyStatement) {
    var description = fireflyStatement.getDescription();
    if (!description.startsWith("ACH C-") || !description.startsWith("TATA MOTORS LTD ORD DIV")) {
      return;
    }
    fireflyStatement.addTag("stock");
    fireflyStatement.setCategory("stock");

    String[] split = description.split("-");
    if (split.length <= 1) {
      return;
    }
    if (description.startsWith("TATA MOTORS LTD ORD DIV")) {
      fireflyStatement.setOpposingAccount("Tata Motors Ltd");
    } else {
      fireflyStatement.setOpposingAccount(split[1]);
    }
    fireflyStatement.addTag("dividend");
  }

  private static void parseAndPopulateMisc(FireflyStatement fireflyStatement) {
    String description = fireflyStatement.getDescription();
    if (description.startsWith("POS")) {
      String[] split = description.split("\\s+");
      if (split.length <= 3) {
        return;
      }
      String opAccName = split[2];
      fireflyStatement.setOpposingAccount(opAccName);
    }
  }

  private static void parseAndPopulateHdfc(FireflyStatement fireflyStatement) {
    var description = fireflyStatement.getDescription();
    if (description.startsWith("FD THROUGH") || description.startsWith(
        "PRIN AND INT AUTO_REDEEM")) {
      fireflyStatement.setOpposingAccount("HDFC FD");
      fireflyStatement.addTag("bank");
    }
    if (description.contains("RELOAD FOREX CARD")) {
      fireflyStatement.setOpposingAccount("HDFC Forex Card");
      fireflyStatement.addTag("forex_card").addTag("wallet").addTag("bank");
    }
    if (description.startsWith("CC") && description.contains(
        "AUTOPAY")) {
      fireflyStatement.setOpposingAccount("HDFC Bank");
      fireflyStatement.addTag("credit_card").addTag("bank");
      fireflyStatement.setCategory("bill");
    }
    if (description.startsWith("CREDIT INTEREST CAPITALISED")) {
      fireflyStatement.setOpposingAccount("HDFC Bank");
      fireflyStatement.addTag("interest").addTag("bank");
    }
    if (description.startsWith("INT. AUTO_REDEMPTION")) {
      fireflyStatement.setOpposingAccount("HDFC Bank");
      fireflyStatement.addTag("interest").addTag("bank");
    }
  }

  private static void parseAndPopulateSalary(FireflyStatement fireflyStatement) {
    String description = fireflyStatement.getDescription();
    if (description.contains("BLUEOPTIMA")) {
      fireflyStatement.setOpposingAccount("Blueoptima");
      fireflyStatement.addTag("salary");
      fireflyStatement.setCategory("salary");
    }
  }

  private static void parseAndPopulateUpi(FireflyStatement fireflyStatement) {
    var description = fireflyStatement.getDescription();
    if (!description.startsWith("UPI-")) {
      return;
    }
    fireflyStatement.addTag("UPI");

    String[] split = description.split("-");
    if (split.length <= 1) {
      return;
    }
    String opAccName = split[1];
    if (opAccName.equals("ADD MONEY TO WALLET")) {
      fireflyStatement.setOpposingAccount("PAYTM");
      fireflyStatement.addTag("wallet");
    } else {
      fireflyStatement.setOpposingAccount(opAccName);
    }

    if (opAccName.toUpperCase().startsWith("ZOMATO") || (
        opAccName.toUpperCase().startsWith("SWIGGY") &&
            !opAccName.toUpperCase().contains("SWIGGYINSTAMART"))) {
      fireflyStatement.setCategory("food");
    }
    if (opAccName.toUpperCase().startsWith(
        "BLINKIT") || opAccName.toUpperCase().startsWith(
        "SWIGGYINSTAMART") || opAccName.toUpperCase().startsWith("GROFERS")) {
      fireflyStatement.setCategory("grocery");
    }

    if (opAccName.toUpperCase().startsWith("JIOMOBILITY")
        || opAccName.toUpperCase().startsWith("PAYTM RECHARGE")) {
      fireflyStatement.setCategory("mobile_recharge");
    }
  }

}
