package com.sohardh.account.job.task;

import com.sohardh.account.service.upload.UploadService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class UploadStatementTask implements Tasklet {

  private final UploadService uploadService;

  public UploadStatementTask(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    uploadService.uploadStatements();
    return RepeatStatus.FINISHED;
  }
}
