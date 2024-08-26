package com.sohardh.account.job.task;

import com.sohardh.account.service.category.CategorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CategorizationTask  implements Tasklet {

  private final CategorizationService categorizationService;

  public CategorizationTask(CategorizationService categorizationService) {
    this.categorizationService = categorizationService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    categorizationService.categorize();
    return RepeatStatus.FINISHED;
  }
}
