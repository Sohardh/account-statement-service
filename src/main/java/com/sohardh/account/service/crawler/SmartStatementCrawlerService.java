package com.sohardh.account.service.crawler;

import com.sohardh.account.dto.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

  public SmartStatementCrawlerService(WebDriver webDriver) {
    this.webDriver = webDriver;
  }


  public List<Statement> getStatements(String url) {

    log.info("Crawling statements site..");
    try {
      // Navigate to Login page
      login(url);

      // Get statement url
      var table = getTable();
      if (table.isEmpty()) {
        return Collections.emptyList();
      }

      String tableHtml = table.get();

      return getStatementsUsingTable(tableHtml);

    } catch (Exception e) {
      log.error("An exception occurred while crawling statements site.", e);
      return Collections.emptyList();
    } finally {
      webDriver.quit();
    }
  }

  public List<Statement> getStatementsUsingTable(String tableHtml) {
    ArrayList<Statement> statements = new ArrayList<>();
    for (Element tr : Jsoup.parse(tableHtml).select("tr")) {
      Elements tds = tr.select("td");
      if (tds.size() >= 7) {
        Statement statement = new Statement(
            tds.get(0).text(),     // getDate(tds.get(0).text()),
            tds.get(1).text(),
            tds.get(2).text(),
            tds.get(3).text(),
            parseDouble(tds.get(4).text()),
            parseDouble(tds.get(5).text()),
            parseDouble(tds.get(6).text())
        );
        statements.add(statement);
      }
    }
    return statements;
  }

  private static Date getDate(String date) {
    try {
      return new SimpleDateFormat("dd/MM/yyyy").parse(date);
    } catch (ParseException e) {
      log.error("Exception occurred while parsing date", e);
      throw new RuntimeException(e);
    }
  }

  private Optional<String> getTable() {

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

}
