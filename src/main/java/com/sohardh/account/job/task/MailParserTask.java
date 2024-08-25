package com.sohardh.account.job.task;


import static com.sohardh.account.util.DateUtil.YYYY_MM_DD;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.sohardh.account.service.mail.parser.MailParserService;
import com.sohardh.account.util.DateUtil;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
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

  public MailParserTask(MailParserService mailParserService) {
    this.mailParserService = mailParserService;
  }

  @Override
  @Transactional(value = TxType.REQUIRES_NEW)
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    var context = chunkContext.getStepContext().getStepExecution().getJobExecution()
        .getExecutionContext();
    var lastDateString = (String) context.get("fetchDate");
    if (context.isEmpty() || isEmpty(lastDateString)) {
      lastDateString = "2024-04-01";
    }
    var lastDate = DateUtil.parseDate(lastDateString, YYYY_MM_DD);
    mailParserService.parseAndSaveStatementLinks(lastDate);
    context.put("fetchDate", DateUtil.convertToString(DateUtil.getNextMonth(lastDate), YYYY_MM_DD));
    return RepeatStatus.FINISHED;
  }
}
