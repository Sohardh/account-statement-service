package com.sohardh.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "job_statement_urls", schema = "account_service")
@NoArgsConstructor
public class JobStatementUrlModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_job_statement_urls", nullable = false)
  private Long id;

  @Column(name = "tx_url", nullable = false)
  private String url;

  @Column(name = "ts_created_at", nullable = false)
  private LocalDate createdAt;

  @Column(name = "bl_processed", nullable = false)
  private Boolean isProcessed = false;

  @Column(name = "ts_processed_at")
  private LocalDate processedAt;

  public JobStatementUrlModel(String url) {
    this.url = url;
    this.createdAt = LocalDate.now();
  }
}