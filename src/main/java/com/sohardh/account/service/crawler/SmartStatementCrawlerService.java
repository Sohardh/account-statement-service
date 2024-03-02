package com.sohardh.account.service.crawler;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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


  public Optional<String> getStatementTable(String url) {

    log.info("Crawling statements site..");
    try {
      // Navigate to Login page
      login(url);

      // Get statement url
      return getTable();

    } catch (Exception e) {
      log.error("An exception occurred while crawling statements site.", e);
      return Optional.empty();
    } finally {
      webDriver.quit();
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

}
