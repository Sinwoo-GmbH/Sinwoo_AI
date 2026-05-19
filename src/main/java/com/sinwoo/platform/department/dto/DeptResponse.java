package com.sinwoo.platform.department.dto;

import com.sinwoo.platform.department.domain.Dept;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public record DeptResponse(
        Long deptId,
        Long tenantId,
        Long coId,
        String deptCd,
        String deptNm,
        Long upDeptId,
        Integer deptLvlNo,
        String stsCd,
        String regionCd,
        BigDecimal vacCnt,
        BigDecimal vacInc,
        Integer dspOrd,
        OffsetDateTime crtDtm,
        OffsetDateTime updDtm
) {
    public static DeptResponse from(Dept dept) {
        return new DeptResponse(
                dept.getId(), dept.getTenantId(), dept.getCoId(),
                dept.getDeptCd(), dept.getDeptNm(), dept.getUpDeptId(),
                dept.getDeptLvlNo(), dept.getStsCd(),
                dept.getRegionCd(), dept.getVacCnt(), dept.getVacInc(), dept.getDspOrd(),
                dept.getCreatedAt(), dept.getUpdatedAt()
        );
    }

    // ── nested: tree node ──
    public record Node(
            Long deptId, String deptCd, String deptNm,
            Long upDeptId, Integer deptLvlNo, String stsCd,
            String regionCd, BigDecimal vacCnt, BigDecimal vacInc, Integer dspOrd,
            List<Node> childList
    ) {
        public static Node from(Dept dept) {
            return new Node(
                    dept.getId(), dept.getDeptCd(), dept.getDeptNm(),
                    dept.getUpDeptId(), dept.getDeptLvlNo(), dept.getStsCd(),
                    dept.getRegionCd(), dept.getVacCnt(), dept.getVacInc(), dept.getDspOrd(),
                    new ArrayList<>()
            );
        }
    }

    // ── nested: list wrapper ──
    public record ListWrap(int totCnt, List<DeptResponse> itemList) {}

    // ── nested: tree wrapper ──
    public record TreeWrap(int totCnt, List<Node> itemList) {}

    // ── nested: employee count ──
    public record EmpCount(Long deptId, int empCnt) {}
}
