package com.sinwoo.platform.wrktm.repository;

import com.sinwoo.platform.wrktm.domain.WrkTm;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WrkTmRepository extends JpaRepository<WrkTm, Long> {

    @Query("SELECT w FROM WrkTm w WHERE w.tenantId = :tid AND w.coId = :cid AND w.empId = :eid AND w.workDt = :dt AND w.delYn = :del")
    Optional<WrkTm> findOne(@Param("tid") Long tid, @Param("cid") Long cid, @Param("eid") Long eid, @Param("dt") LocalDate dt, @Param("del") String del);

    @Query("SELECT w FROM WrkTm w WHERE w.tenantId = :tid AND w.coId = :cid AND w.empId = :eid AND w.workDt BETWEEN :from AND :to AND w.delYn = :del ORDER BY w.workDt ASC")
    List<WrkTm> findByEmpPeriod(@Param("tid") Long tid, @Param("cid") Long cid, @Param("eid") Long eid,
            @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("del") String del);

    @Query("SELECT w FROM WrkTm w WHERE w.tenantId = :tid AND w.coId = :cid AND w.workDt BETWEEN :from AND :to AND w.delYn = :del ORDER BY w.empId ASC, w.workDt ASC")
    List<WrkTm> findByCoPeroid(@Param("tid") Long tid, @Param("cid") Long cid,
            @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("del") String del);

    @Query("SELECT w FROM WrkTm w WHERE w.id = :id AND w.tenantId = :tid AND w.coId = :cid")
    Optional<WrkTm> findOneById(@Param("id") Long id, @Param("tid") Long tid, @Param("cid") Long cid);
}
