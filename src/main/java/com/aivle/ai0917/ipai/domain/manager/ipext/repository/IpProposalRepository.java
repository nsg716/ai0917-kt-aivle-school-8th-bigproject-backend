package com.aivle.ai0917.ipai.domain.manager.ipext.repository;

import com.aivle.ai0917.ipai.domain.manager.ipext.model.IpProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IpProposalRepository extends JpaRepository<IpProposal, Long> {

    // 목록 조회 (삭제되지 않은 것만)
    @Query("SELECT p FROM IpProposal p WHERE p.status <> 'DELETED'")
    Page<IpProposal> findAllActive(Pageable pageable);

    // [상세 조회] ID가 일치하고 DELETED 상태가 아닌 것만 조회
    @Query("SELECT p FROM IpProposal p WHERE p.id = :id AND p.status <> 'DELETED'")
    Optional<IpProposal> findActiveById(@Param("id") Long id);
}