package com.sohardh.account.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class WebDriverConfig {

  @Bean
  public WebDriver getChromeDriver() {
    try {
      var options = new ChromeOptions();
      options.addArguments("--headless");
      options.addArguments("--no-sandbox");
      options.addArguments("--disable-dev-shm-usage");
      options.addArguments("--disable-extensions");
      WebDriverManager.chromedriver().setup();
      return new ChromeDriver(options);
    } catch (Exception e) {
      log.error("Failed to initialize chrome driver", e);
      throw e;
    }
  }

}
