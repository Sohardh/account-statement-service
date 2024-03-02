package com.sohardh.account.service.mail.parser;

import static io.micrometer.common.util.StringUtils.isEmpty;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailParserService {

  public Optional<String> getStatementLink(Optional<String> emailBody) {
    if (emailBody.isEmpty() || isEmpty(emailBody.get())) {
      log.info("Email Body is empty. Skipping parsing!");
      return Optional.empty();
    }

    var doc = Jsoup.parse(emailBody.get());

    // Find anchor tags with the specified text content
    var links = doc.select("a > strong");

    if (links.isEmpty()) {
      log.info("No anchor tag found with text 'View your SmartStatement'");
      return Optional.empty();
    }
    return Optional.of(Objects.requireNonNull(links.get(0).parent()).attr("href"));
  }
}
