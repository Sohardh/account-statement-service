package com.sohardh.account.repositories;

import com.sohardh.account.model.JobStatementContextModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobStatementContextRepository extends
    JpaRepository<JobStatementContextModel, String> {

  @Query("from JobStatementContextModel j where j.txJobName = ?1")
  JobStatementContextModel findByJobName(String jobName);

}
