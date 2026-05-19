package com.sinwoo.platform.leave.repository;

import com.sinwoo.platform.leave.domain.LeaveGrant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveGrantRepository extends JpaRepository<LeaveGrant, Long> {

    @Query("SELECT g FROM LeaveGrant g WHERE g.tenantId = :tid AND g.coId = :cid AND g.empId = :eid AND g.grantYr = :yr AND g.delYn = :del")
    Optional<LeaveGrant> findOne(@Param("tid") String tid, @Param("cid") String cid, @Param("eid") String eid, @Param("yr") Short yr, @Param("del") String del);

    @Query("SELECT g FROM LeaveGrant g WHERE g.tenantId = :tid AND g.coId = :cid AND g.empId = :eid AND g.delYn = :del ORDER BY g.grantYr DESC")
    List<LeaveGrant> findByEmp(@Param("tid") String tid, @Param("cid") String cid, @Param("eid") String eid, @Param("del") String del);

    @Query("SELECT g FROM LeaveGrant g WHERE g.tenantId = :tid AND g.coId = :cid AND g.grantYr = :yr AND g.delYn = :del ORDER BY g.empId ASC")
    List<LeaveGrant> findByCoYear(@Param("tid") String tid, @Param("cid") String cid, @Param("yr") Short yr, @Param("del") String del);
}
