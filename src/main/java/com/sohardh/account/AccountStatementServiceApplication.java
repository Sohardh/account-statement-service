package com.sohardh.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AccountStatementServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountStatementServiceApplication.class, args);
  }

//  @Component
//  @Slf4j
//  public static class MyApplicationRunner implements ApplicationRunner {
//
//    private final AccountStatementProcessor accountStatementProcessor;
//
//    public MyApplicationRunner(AccountStatementProcessor accountStatementProcessor) {
//      this.accountStatementProcessor = accountStatementProcessor;
//    }
//
//
//    @Override
//    public void run(ApplicationArguments args) throws GeneralSecurityException, IOException {
////      accountStatementProcessor.processAccountStatements();
//    }
//  }

}
