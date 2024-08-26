package com.sohardh.account.repositories;

import com.sohardh.account.model.StatementModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatementRepository extends JpaRepository<StatementModel, Long> {


  @Query("from StatementModel s where s.refNo in (?1)")
  List<StatementModel> findAllByRefNo(List<String> refNo);

  @Query("from StatementModel s where s.isProcessed = false")
  List<StatementModel> findAllNotProcessed();

}
