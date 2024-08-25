package com.sohardh.account.model;

import com.sohardh.account.dto.Statement;
import com.sohardh.account.util.DateUtil;
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

@Entity
@Table(schema = "account_service", name = "statements")
@Getter
@Setter
@NoArgsConstructor
public class StatementModel {

  @Id
  @Column(name = "id_statement")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long statementId;

  @Column(name = "ts_date", nullable = false)
  private LocalDate date;

  @Column(name = "tx_description", nullable = false)
  private String description;

  @Column(name = "ts_value_date", nullable = false)
  private LocalDate valueDate;

  @Column(name = "tx_ref_no", nullable = false, unique = true)
  private String refNo;

  @Column(name = "nu_debit")
  private Double debit;

  @Column(name = "nu_credit")
  private Double credit;

  @Column(name = "nu_closing_balance", nullable = false)
  private Double closingBalance;

  public StatementModel(Statement statement) {
    this.date = DateUtil.parseDate(statement.date(),"dd/MM/yyyy");
    this.valueDate = DateUtil.parseDate(statement.valueDate(),"dd-MM-yyyy");
    this.description = statement.description();
    this.refNo = statement.refNo();
    this.debit = statement.debit();
    this.credit = statement.credit();
    this.closingBalance = statement.closingBalance();
  }
}
