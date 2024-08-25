package com.sohardh.account.repositories;

import com.sohardh.account.model.JobStatementUrlModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobStatementUrlRepository extends JpaRepository<JobStatementUrlModel, Long> {

  @Query("from JobStatementUrlModel s where s.url in (?1)")
  List<JobStatementUrlModel> findAllByUrl(List<String> urls);

}
