package com.sinwoo.platform.aprv.repository;

import com.sinwoo.platform.aprv.domain.AprvLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AprvLineRepository extends JpaRepository<AprvLine, Long> {

    @Query("SELECT a FROM AprvLine a WHERE a.reqTpCd = :tp AND a.reqId = :rid AND a.delYn = :del ORDER BY a.stepOrder ASC")
    List<AprvLine> findByReq(@Param("tp") String tp, @Param("rid") Long rid, @Param("del") String del);

    @Query("SELECT a FROM AprvLine a WHERE a.empId = :eid AND a.stsCd = :sts AND a.delYn = :del ORDER BY a.reqId DESC")
    List<AprvLine> findByEmpSts(@Param("eid") String eid, @Param("sts") String sts, @Param("del") String del);

    @Query("SELECT a FROM AprvLine a WHERE a.id = :id AND a.tenantId = :tid AND a.coId = :cid AND a.delYn = :del")
    Optional<AprvLine> findOne(@Param("id") Long id, @Param("tid") String tid, @Param("cid") String cid, @Param("del") String del);

    void deleteAllByReqTpCdAndReqId(String reqTpCd, Long reqId);
}
