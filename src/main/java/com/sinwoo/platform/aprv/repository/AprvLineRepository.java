package com.sinwoo.platform.aprv.repository;

import com.sinwoo.platform.aprv.domain.AprvLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AprvLineRepository extends JpaRepository<AprvLine, Long> {

    List<AprvLine> findAllByReqTpCdAndReqIdAndDelYnOrderByStepOrderAsc(
            String reqTpCd, Long reqId, String delYn);

    List<AprvLine> findAllByEmpIdAndStsCdAndDelYnOrderByReqIdDesc(
            String empId, String stsCd, String delYn);

    Optional<AprvLine> findByIdAndTenantIdAndCoIdAndDelYn(
            Long id, String tenantId, String coId, String delYn);

    void deleteAllByReqTpCdAndReqId(String reqTpCd, Long reqId);
}
