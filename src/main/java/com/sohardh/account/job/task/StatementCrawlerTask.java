package com.sohardh.account.job.task;

import com.sohardh.account.dto.Statement;
import com.sohardh.account.model.JobStatementUrlModel;
import com.sohardh.account.model.StatementModel;
import com.sohardh.account.repositories.JobStatementUrlRepository;
import com.sohardh.account.repositories.StatementRepository;
import com.sohardh.account.service.crawler.SmartStatementCrawlerService;
import com.sohardh.account.util.CommonUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatementCrawlerTask implements Tasklet {

  private final SmartStatementCrawlerService smartStatementCrawlerService;
  private final JobStatementUrlRepository jobStatementUrlRepository;
  private final StatementRepository statementRepository;

  public StatementCrawlerTask(SmartStatementCrawlerService smartStatementCrawlerService,
      JobStatementUrlRepository jobStatementUrlRepository,
      StatementRepository statementRepository) {
    this.smartStatementCrawlerService = smartStatementCrawlerService;
    this.jobStatementUrlRepository = jobStatementUrlRepository;
    this.statementRepository = statementRepository;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {

    // As the statements are month wise, the list will never be huge.
    var urls = jobStatementUrlRepository.findAllNotProcessed();

    for (JobStatementUrlModel model : urls) {
      List<Statement> statements;
      try {
        statements = smartStatementCrawlerService.getStatements(model);
      } catch (Exception e) {
        log.error("An exception occurred while crawling statements site.", e);
        continue;
      }

      var internalReferences = statements.stream().map(CommonUtil::getInternalReference)
          .toList();
      var existingDesc = statementRepository.findAllByInternalReference(internalReferences);
      if (!existingDesc.isEmpty()) {
        log.info("{} statements are existing. Skipping them.", existingDesc.size());
      }

      var newStatements = statements.stream()
          .filter(statement -> existingDesc.stream().noneMatch(
              existing -> Objects.equals(existing.getRefNo(), statement.refNo())
                  && Objects.equals(existing.getInternalReference(),
                  CommonUtil.getInternalReference(statement))))
          .map(StatementModel::new)
          .toList();
      log.info("Found {} new statements. Saving them...", newStatements.size());
      statementRepository.saveAll(newStatements);
      model.setIsProcessed(true);
      model.setProcessedAt(LocalDate.now());
      jobStatementUrlRepository.save(model);
      // throttle subsequent requests
      Thread.sleep(5_000);
    }

    return RepeatStatus.FINISHED;
  }
}
