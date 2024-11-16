package com.sohardh.account.job;

import com.sohardh.account.job.task.CategorizationTask;
import com.sohardh.account.job.task.MailParserTask;
import com.sohardh.account.job.task.StatementCrawlerTask;
import com.sohardh.account.job.task.UploadStatementTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class JobConfiguration {

  public static final String ACCOUNT_STATEMENT_SERVICE_JOB = "accountStatementServiceJob";

  @Bean
  public Job accountStatementServiceJob(JobRepository jobRepository, Step mailParserStep,
      Step statementCrawlerStep, Step categorizationStep, Step uploadStatementStep) {
    return new JobBuilder(ACCOUNT_STATEMENT_SERVICE_JOB, jobRepository)
        .start(mailParserStep)
        .next(statementCrawlerStep)
        .next(categorizationStep)
        .next(uploadStatementStep)
        .build();
  }

  @Bean
  public Step mailParserStep(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager, MailParserTask task) {
    return new StepBuilder("mailParserStep", jobRepository)
        .tasklet(task, platformTransactionManager)
        .build();
  }

  @Bean
  public Step statementCrawlerStep(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager, StatementCrawlerTask task) {
    return new StepBuilder("statementCrawlerStep", jobRepository)
        .tasklet(task, platformTransactionManager)
        .build();
  }

  @Bean
  public Step categorizationStep(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager, CategorizationTask task) {
    return new StepBuilder("categorizationStep", jobRepository)
        .tasklet(task, platformTransactionManager)
        .build();
  }


  @Bean
  public Step uploadStatementStep(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager, UploadStatementTask task) {
    return new StepBuilder("uploadStatementStep", jobRepository)
        .tasklet(task, platformTransactionManager)
        .build();
  }

}
