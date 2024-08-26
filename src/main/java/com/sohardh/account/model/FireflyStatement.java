package com.sohardh.account.model;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "firefly_statements", schema = "account_service")
@NoArgsConstructor
public class FireflyStatement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_firefly_statements", nullable = false)
  private Long id;

  @Column(name = "ts_date", nullable = false)
  private LocalDate date;

  @Column(name = "tx_description", nullable = false)
  private String description;

  @Column(name = "tx_internal_reference", nullable = false)
  private String internalReference;

  @Column(name = "nu_debit")
  private Double debit;

  @Column(name = "nu_credit")
  private Double credit;

  @Column(name = "bl_processed", nullable = false)
  private Boolean isProcessed = false;

  @Column(name = "ts_processed_at")
  private LocalDate processedAt;

  @Column(name = "arr_tags")
  @Type(StringArrayType.class)
  private List<String> tags;

  @Column(name = "tx_category")
  private String category;

  @Column(name = "tx_opposing_account", nullable = false)
  private String opposingAccount;

  @Column(name = "tx_asset_account")
  private String assetAccount;

  public FireflyStatement(StatementModel statementModel) {
    this.description = statementModel.getDescription();
    this.date = statementModel.getValueDate();
    this.debit = statementModel.getDebit();
    this.credit = statementModel.getCredit();
    this.internalReference = UUID.randomUUID() + "-" + statementModel.getRefNo();
  }
  public FireflyStatement addTag(String tag) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }
    this.tags.add(tag);
    return this;
  }
}