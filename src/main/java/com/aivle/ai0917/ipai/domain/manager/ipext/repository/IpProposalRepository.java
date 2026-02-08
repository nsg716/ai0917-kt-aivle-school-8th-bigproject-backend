package com.aivle.ai0917.ipai.domain.manager.ipext.repository;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IpProposalRepository extends JpaRepository<IpProposal, Long> {

    // [기존] 전체 목록 조회 (삭제되지 않은 것만) - 필요시 유지 또는 제거
    @Query("SELECT p FROM IpProposal p WHERE p.status <> 'DELETED'")
    Page<IpProposal> findAllActive(Pageable pageable);

    // [수정] 특정 Manager의 목록 조회 (삭제되지 않은 것만)
    @Query("SELECT p FROM IpProposal p WHERE p.managerId = :managerId AND p.status <> 'DELETED'")
    Page<IpProposal> findAllActiveByManagerId(@Param("managerId") String managerId, Pageable pageable);

    // [기존] ID로 조회
    @Query("SELECT p FROM IpProposal p WHERE p.id = :id AND p.status <> 'DELETED'")
    Optional<IpProposal> findActiveById(@Param("id") Long id);

    // [수정] ID와 ManagerId로 상세 조회 (본인 제안서만 접근 가능하도록)
    @Query("SELECT p FROM IpProposal p WHERE p.id = :id AND p.managerId = :managerId AND p.status <> 'DELETED'")
    Optional<IpProposal> findActiveByIdAndManagerId(@Param("id") Long id, @Param("managerId") String managerId);
}