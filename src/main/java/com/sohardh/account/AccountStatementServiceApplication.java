package com.sohardh.account;

import com.sohardh.account.service.MailService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AccountStatementServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountStatementServiceApplication.class, args);
  }

  @Component
  public static class MyApplicationRunner implements ApplicationRunner {

    private final MailService mailService;

    public MyApplicationRunner(@Qualifier("GmailServiceImpl") MailService mailService) {
      this.mailService = mailService;
    }


    @Override
    public void run(ApplicationArguments args) throws GeneralSecurityException, IOException {

      mailService.getEmailBody("");
    }
  }

}
