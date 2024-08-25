package com.sohardh.account.job;

import com.sohardh.account.repositories.StatementRepository;
import com.sohardh.account.service.crawler.SmartStatementCrawlerService;
import com.sohardh.account.service.mail.MailService;
import com.sohardh.account.service.mail.parser.MailParserService;
import com.sohardh.account.util.DateUtil;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountStatementProcessor {


  private final MailService mailService;
  private final MailParserService mailParserService;
  private final SmartStatementCrawlerService smartStatementCrawlerService;
  private final StatementRepository statementRepository;

  public AccountStatementProcessor(@Qualifier("GmailServiceImpl") MailService mailService,
      MailParserService mailParserService,
      SmartStatementCrawlerService smartStatementCrawlerService,
      StatementRepository statementRepository) {
    this.mailService = mailService;
    this.mailParserService = mailParserService;
    this.smartStatementCrawlerService = smartStatementCrawlerService;
    this.statementRepository = statementRepository;
  }

  public void processAccountStatements() throws IOException, GeneralSecurityException {

    var emailBody = mailService.getEmailBodies(DateUtil.parseDate("01-01-2024","dd-MM-yyyy"));
//    var statementLink = mailParserService.saveStatementLinks(lastDate);
//    if (statementLink.isEmpty()) {
//      log.info("Statement Link not found!");
//      return;
//    }
//    log.info("Statement Link : {}", statementLink.get());
//    var statements = smartStatementCrawlerService.getStatements(statementLink.get());
//    var refNos = statements.stream().map(Statement::refNo).toList();
//
//    var existingRefNos = statementRepository.findAllByRefNo(refNos).stream()
//        .map(StatementModel::getRefNo).toList();
//
//    log.info("{} statements are existing. Skipping them.", existingRefNos.size());
//
//    var newStatements = statements.stream()
//        .filter(statement -> existingRefNos.contains(statement.refNo()))
//        .map(StatementModel::new)
//        .toList();
//    log.info("Found {} new statements. Saving them...", newStatements.size());
//    statementRepository.saveAll(newStatements);
  }

}
