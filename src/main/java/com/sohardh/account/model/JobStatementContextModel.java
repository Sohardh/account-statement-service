package com.sohardh.account.model;

import com.google.gson.Gson;
import com.sohardh.account.dto.JobContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Entity
@Table(name = "job_statement_context", schema = "account_service")
public class JobStatementContextModel {

  @Getter
  @Id
  @Column(name = "tx_job_name", nullable = false)
  private String txJobName;

  @Column(name = "js_context")
  private String context;

  public JobContext getContext() {
    return new Gson().fromJson(context, JobContext.class);
  }
}