package com.sohardh.account.service.mail.parser;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.sohardh.account.model.JobStatementUrlModel;
import com.sohardh.account.repositories.JobStatementUrlRepository;
import com.sohardh.account.service.mail.MailService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailParserService {

  private final MailService mailService;
  private final JobStatementUrlRepository jobStatementUrlRepository;

  public MailParserService(@Qualifier("GmailServiceImpl") MailService mailService,
      JobStatementUrlRepository jobStatementUrlRepository) {
    this.mailService = mailService;
    this.jobStatementUrlRepository = jobStatementUrlRepository;
  }

  public boolean parseAndSaveStatementLinks(LocalDate lastDate) {
    List<String> emails;
    try {
      emails = mailService.getEmailBodies(lastDate);
    } catch (Exception e) {
      log.error("There was an error while fetching email bodies", e);
      return false;
    }
    log.info("Parsing {} mails.", emails.size());
    var linksList = new ArrayList<String>();
    for (String emailBody : emails) {
      if (emailBody.isEmpty() || isEmpty(emailBody)) {
        log.info("Email Body is empty. Skipping parsing!");
        return false;
      }

      var doc = Jsoup.parse(emailBody);

      // Find anchor tags with the specified text content
      var links = doc.select("a > strong");

      if (links.isEmpty()) {
        log.info("No anchor tag found with text 'View your SmartStatement'");
        return false;
      }
      String href = Objects.requireNonNull(links.get(0).parent()).attr("href");
      linksList.add(href);
    }

    var existingUrls = jobStatementUrlRepository.findAllByUrl(linksList).stream()
        .map(JobStatementUrlModel::getUrl).toList();

    var urls = linksList.stream().filter(link -> !existingUrls.contains(link))
        .map(JobStatementUrlModel::new).toList();
    if (urls.isEmpty()) {
      log.info("No new urls found. Skipping save!");
      return false;
    }
    log.info("Saving {} newly found Urls.", urls.size());
    jobStatementUrlRepository.saveAll(urls);
    return true;
  }
}
