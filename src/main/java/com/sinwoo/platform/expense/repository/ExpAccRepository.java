package com.sinwoo.platform.expense.repository;

import com.sinwoo.platform.expense.domain.ExpAcc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpAccRepository extends JpaRepository<ExpAcc, Long> {

    @Query("SELECT a FROM ExpAcc a WHERE a.tenantId = :tid AND a.coId = :cid AND a.delYn = :del ORDER BY a.dspOrd ASC, a.expAccCd ASC")
    List<ExpAcc> findByCo(@Param("tid") Long tid, @Param("cid") Long cid, @Param("del") String del);

    @Query("SELECT a FROM ExpAcc a WHERE a.tenantId = :tid AND a.coId = :cid AND a.expAccCd = :cd")
    Optional<ExpAcc> findOne(@Param("tid") Long tid, @Param("cid") Long cid, @Param("cd") Integer cd);
}
