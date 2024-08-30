package com.sohardh.account.repositories;

import com.sohardh.account.model.FireflyStatement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FireflyStatementRepository extends JpaRepository<FireflyStatement, Long> {

  @Query("from FireflyStatement f where f.internalReference in (?1)")
  List<FireflyStatement> getAllByInternalReferences(List<String> internalReferences);

  @Query("from FireflyStatement f where f.isProcessed = false ")
  List<FireflyStatement> findAllNotProcessed();
}
