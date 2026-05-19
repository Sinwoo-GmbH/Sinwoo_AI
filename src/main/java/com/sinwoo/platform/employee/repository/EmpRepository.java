package com.sinwoo.platform.employee.repository;

import com.sinwoo.platform.employee.domain.Emp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpRepository extends JpaRepository<Emp, Long> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Emp e WHERE e.tenantId = :tid AND e.coId = :cid AND UPPER(e.empNo) = UPPER(:no)")
    boolean existsByNo(@Param("tid") Long tid, @Param("cid") Long cid, @Param("no") String no);

    boolean existsByUsrId(Long usrId);

    Optional<Emp> findByUsrId(Long usrId);

    @Query("SELECT e FROM Emp e WHERE e.tenantId = :tid ORDER BY e.empNm ASC, e.id ASC")
    List<Emp> findByTenant(@Param("tid") Long tid);

    @Query("SELECT e FROM Emp e WHERE e.tenantId = :tid AND e.coId = :cid ORDER BY e.empNm ASC, e.id ASC")
    List<Emp> findByCo(@Param("tid") Long tid, @Param("cid") Long cid);

    @Query("SELECT e FROM Emp e WHERE e.tenantId = :tid AND e.coId IN :cids ORDER BY e.empNm ASC, e.id ASC")
    List<Emp> findByCoIds(@Param("tid") Long tid, @Param("cids") Collection<Long> cids);

    @Query("SELECT e FROM Emp e WHERE e.tenantId = :tid AND e.coId = :cid AND e.deptId = :did ORDER BY e.empNm ASC, e.id ASC")
    List<Emp> findByDept(@Param("tid") Long tid, @Param("cid") Long cid, @Param("did") Long did);

    @Query("SELECT e FROM Emp e WHERE e.id = :id AND e.tenantId = :tid AND e.coId = :cid")
    Optional<Emp> findOne(@Param("id") Long id, @Param("tid") Long tid, @Param("cid") Long cid);
}
