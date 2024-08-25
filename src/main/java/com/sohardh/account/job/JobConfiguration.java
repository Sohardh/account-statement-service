package com.sohardh.account.job;

import com.sohardh.account.job.task.MailParserTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@EnableBatchProcessing
public class JobConfiguration {

  @Bean
  public Job accountStatementServiceJob(JobRepository jobRepository, Step mailParserStep) {
    return new JobBuilder("accountStatementServiceJob", jobRepository)
        .start(mailParserStep)
        .build();
  }

  @Bean
  public Step mailParserStep(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager, MailParserTask mailParserTask) {

    return new StepBuilder("mailParserStep", jobRepository)
        .tasklet(mailParserTask, platformTransactionManager)
        .build();

  }

}
