package com.sinwoo.platform.leave.repository;

import com.sinwoo.platform.leave.domain.LeaveReq;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveReqRepository extends JpaRepository<LeaveReq, Long> {

    @Query("SELECT r FROM LeaveReq r WHERE r.id = :id AND r.tenantId = :tid AND r.coId = :cid AND r.delYn = :del")
    Optional<LeaveReq> findOne(@Param("id") Long id, @Param("tid") String tid, @Param("cid") String cid, @Param("del") String del);

    @Query("SELECT r FROM LeaveReq r WHERE r.tenantId = :tid AND r.coId = :cid AND r.empId = :eid AND r.delYn = :del ORDER BY r.strDt DESC")
    List<LeaveReq> findByEmp(@Param("tid") String tid, @Param("cid") String cid, @Param("eid") String eid, @Param("del") String del);

    @Query("SELECT r FROM LeaveReq r WHERE r.tenantId = :tid AND r.coId = :cid AND r.empId = :eid"
            + " AND r.strDt >= :from AND r.endDt <= :to AND r.delYn = :del ORDER BY r.strDt DESC")
    List<LeaveReq> findByEmpPeriod(@Param("tid") String tid, @Param("cid") String cid, @Param("eid") String eid,
            @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("del") String del);

    @Query("SELECT r FROM LeaveReq r WHERE r.tenantId = :tid AND r.coId = :cid AND r.delYn = :del ORDER BY r.strDt DESC")
    List<LeaveReq> findByCo(@Param("tid") String tid, @Param("cid") String cid, @Param("del") String del);

    @Query("SELECT r FROM LeaveReq r WHERE r.tenantId = :tid AND r.coId = :cid AND r.empId = :eid"
            + " AND r.strDt <= :endDt AND r.endDt >= :strDt AND r.delYn = :del")
    List<LeaveReq> findOverlapping(@Param("tid") String tid, @Param("cid") String cid, @Param("eid") String eid,
            @Param("strDt") LocalDate strDt, @Param("endDt") LocalDate endDt, @Param("del") String del);
}
