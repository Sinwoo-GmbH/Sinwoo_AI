package com.sinwoo.platform.department.repository;

import com.sinwoo.platform.department.domain.Dept;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeptRepository extends JpaRepository<Dept, Long> {

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dept d WHERE d.tenantId = :tid AND d.coId = :cid AND UPPER(d.deptCd) = UPPER(:cd)")
    boolean existsByCd(@Param("tid") Long tid, @Param("cid") Long cid, @Param("cd") String cd);

    @Query("SELECT d FROM Dept d WHERE d.tenantId = :tid ORDER BY d.deptLvlNo ASC, d.deptNm ASC, d.id ASC")
    List<Dept> findByTenant(@Param("tid") Long tid);

    @Query("SELECT d FROM Dept d WHERE d.tenantId = :tid AND d.coId = :cid ORDER BY d.deptLvlNo ASC, d.deptNm ASC, d.id ASC")
    List<Dept> findByCo(@Param("tid") Long tid, @Param("cid") Long cid);

    @Query("SELECT d FROM Dept d WHERE d.tenantId = :tid AND d.coId IN :cids ORDER BY d.deptLvlNo ASC, d.deptNm ASC, d.id ASC")
    List<Dept> findByCoIds(@Param("tid") Long tid, @Param("cids") Collection<Long> cids);

    @Query("SELECT d FROM Dept d WHERE d.id = :id AND d.tenantId = :tid AND d.coId = :cid")
    Optional<Dept> findOne(@Param("id") Long id, @Param("tid") Long tid, @Param("cid") Long cid);
}
