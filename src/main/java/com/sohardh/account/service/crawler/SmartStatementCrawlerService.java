package com.sohardh.account.service.crawler;

import static io.micrometer.common.util.StringUtils.isBlank;

import com.sohardh.account.dto.Statement;
import com.sohardh.account.model.JobStatementUrlModel;
import com.sohardh.account.repositories.JobStatementUrlRepository;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmartStatementCrawlerService {

  @Value("${hdfc.username}")
  private String hdfcUserName;

  private final WebDriver webDriver;
  private final JobStatementUrlRepository jobStatementUrlRepository;

  public SmartStatementCrawlerService(WebDriver webDriver,
      JobStatementUrlRepository jobStatementUrlRepository) {
    this.webDriver = webDriver;
    this.jobStatementUrlRepository = jobStatementUrlRepository;
  }


  public List<Statement> getStatements(JobStatementUrlModel model) {

    // Get statement table
    var table = getTable(model);
    if (table.isEmpty()) {
      return Collections.emptyList();
    }

    String tableHtml = table.get();
    return getStatementsUsingTable(tableHtml);
  }

  public List<Statement> getStatementsUsingTable(String tableHtml) {
    ArrayList<Statement> statements = new ArrayList<>();
    for (Element tr : Jsoup.parse(tableHtml).select("tr")) {
      Elements tds = tr.select("td");
      if (tds.size() >= 7) {
        Statement statement = new Statement(tds.get(0).text(), tds.get(1).text(), tds.get(2).text(),
            tds.get(3).text(), parseDouble(tds.get(4).text()), parseDouble(tds.get(5).text()),
            parseDouble(tds.get(6).text()));
        statements.add(statement);
      }
    }
    return statements;
  }

  private Optional<String> getTable(JobStatementUrlModel model) {
    // cached
    if (!isBlank(model.getHtmlBody())) {
      log.info("Table was cached in job_statement_urls. Skipping website crawling.");
      return Optional.of(model.getHtmlBody());
    }

    log.info("Crawling statements site with url {}", model.getUrl());

    // Navigate to Login page
    login(model.getUrl());

    // Toggle the statement view
    var viewButton = webDriver.findElement(By.id("View"));
    viewButton.click();

    // Get table div
    var tableDiv = webDriver.findElement(By.id("ACCOUNT_TABLE_LAYER_0"));

    var table = tableDiv.findElement(By.tagName("table"));
    Optional<String> outerHTML = Optional.ofNullable(table.getAttribute("outerHTML"));
    if (outerHTML.isPresent()) {
      log.info("Found the statements table. Website crawled successfully!");
    }

    model.setHtmlBody(outerHTML.orElse(null));
    jobStatementUrlRepository.save(model);
    return outerHTML;
  }

  private void login(String url) {
    webDriver.navigate().to(url);
    var inputField = webDriver.findElement(By.id("pwd"));
    var submitButton = webDriver.findElement(By.id("submit"));

    inputField.sendKeys(hdfcUserName);
    submitButton.click();
    log.info("Login Success.");
  }

  private Double parseDouble(String input) {
    try {
      return Double.parseDouble(input.replaceAll(",", ""));
    } catch (Exception e) {
      return null;
    }
  }

  @PreDestroy
  public void destroy() {
    webDriver.quit();
  }
}
