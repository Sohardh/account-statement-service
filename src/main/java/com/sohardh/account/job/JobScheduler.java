package com.sohardh.account.job;

import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Slf4j
public class JobScheduler {

  private final JobLauncher jobLauncher;
  private final Job accountStatementServiceJob;


  public JobScheduler(JobLauncher jobLauncher, Job accountStatementServiceJob) {
    this.jobLauncher = jobLauncher;
    this.accountStatementServiceJob = accountStatementServiceJob;
  }

  //  @Scheduled(cron = "* * * * */5 ?")
  @Scheduled(fixedDelay = 60_000)
  public void runJob() throws Exception {
    log.info("Starting Account Statement Parser Service Job");
    var start = Instant.now();
    JobParameters params = new JobParametersBuilder()
        .addLong("startTime", System.currentTimeMillis()) // Adds uniqueness
        .toJobParameters();
    jobLauncher.run(accountStatementServiceJob, params);
    var end = Instant.now();
    var dur = Duration.between(start, end);
    log.info("Finished Account Statement Parser Service Job. Execution Time : {}s",
        dur.toSeconds());
  }
}
