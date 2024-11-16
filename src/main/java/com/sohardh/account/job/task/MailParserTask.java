package com.sohardh.account.job.task;


import static com.sohardh.account.job.JobConfiguration.ACCOUNT_STATEMENT_SERVICE_JOB;
import static com.sohardh.account.util.DateUtil.YYYY_MM_DD;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.google.gson.Gson;
import com.sohardh.account.dto.JobContext;
import com.sohardh.account.model.JobStatementContextModel;
import com.sohardh.account.repositories.JobStatementContextRepository;
import com.sohardh.account.service.mail.parser.MailParserService;
import com.sohardh.account.util.DateUtil;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailParserTask implements Tasklet {

  private final MailParserService mailParserService;
  private final JobStatementContextRepository jobStatementContextRepository;

  public MailParserTask(MailParserService mailParserService,
      JobStatementContextRepository jobStatementContextRepository) {
    this.mailParserService = mailParserService;
    this.jobStatementContextRepository = jobStatementContextRepository;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    var context = jobStatementContextRepository.findByJobName(
        ACCOUNT_STATEMENT_SERVICE_JOB);
    var lastDate = DateUtil.parseDate(getLastDateString(context), YYYY_MM_DD);
    log.info("Current fetch date is  : {}", lastDate);
    var isSuccess = mailParserService.parseAndSaveStatementLinks(lastDate);
    if (isSuccess) {
      var fetchDate = DateUtil.convertToString(DateUtil.getNextMonth(lastDate), YYYY_MM_DD);
      log.info("Saving next fetch date: {}", fetchDate);
      saveContext(context, fetchDate);
    }

    return RepeatStatus.FINISHED;
  }

  private void saveContext(JobStatementContextModel context, String fetchDate) {
    if (isNull(context)) {
      context = new JobStatementContextModel();
      context.setTxJobName(ACCOUNT_STATEMENT_SERVICE_JOB);
    }
    context.setContext(new Gson().toJson(new JobContext(fetchDate)));
    jobStatementContextRepository.save(context);
  }

  private static String getLastDateString(JobStatementContextModel context) {
    String lastDateString;
    if (isNull(context) || isNull(context.getContext()) ||
        isEmpty(context.getContext().fetchDate())) {
      lastDateString = DateUtil.convertToString(LocalDate.now(), YYYY_MM_DD);
    } else {
      lastDateString = context.getContext().fetchDate();
    }
    return lastDateString;
  }
}
