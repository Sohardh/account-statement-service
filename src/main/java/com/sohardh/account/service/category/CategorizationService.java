package com.sohardh.account.service.category;

import com.sohardh.account.model.FireflyStatement;
import com.sohardh.account.model.StatementModel;
import com.sohardh.account.repositories.FireflyStatementRepository;
import com.sohardh.account.repositories.StatementRepository;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
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
    var fireflyStatements = statements.stream().map(this::analyzeTransactions).toList();
    fireflyStatementRepository.saveAll(fireflyStatements);
    statementRepository.saveAll(statements);
    log.info("Regex categorization finished. Total categorized statements: {}", fireflyStatements.size());
  }

  private FireflyStatement analyzeTransactions(StatementModel statementModel) {
    var fireflyStatement = new FireflyStatement(statementModel);
    parseAndPopulateUpi(fireflyStatement);
    parseAndPopulateHdfc(fireflyStatement);
    parseAndPopulateSalary(fireflyStatement);
    parseAndPopulateMisc(fireflyStatement);
    statementModel.setIsProcessed(true);
    statementModel.setProcessedAt(LocalDate.now());
    return fireflyStatement;
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
    if (split.length != 2) {
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
