package com.aivle.ai0917.ipai.domain.admin.dashboard.repository;

import com.aivle.ai0917.ipai.domain.admin.dashboard.model.DeploymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentInfoRepository extends JpaRepository<DeploymentInfo, Long> {

    // 가장 최근 배포 정보 조회
    Optional<DeploymentInfo> findTopByOrderByDeploymentTimeDesc();

    // 특정 환경의 최근 배포 조회
    Optional<DeploymentInfo> findTopByEnvironmentOrderByDeploymentTimeDesc(String environment);

    // 특정 버전의 배포 정보 조회
    List<DeploymentInfo> findByVersionOrderByDeploymentTimeDesc(String version);

    /**
     * 특정 시간 이후의 배포 정보 조회 (알림 통합용)
     */
    List<DeploymentInfo> findByDeploymentTimeAfterOrderByDeploymentTimeDesc(LocalDateTime since);
}