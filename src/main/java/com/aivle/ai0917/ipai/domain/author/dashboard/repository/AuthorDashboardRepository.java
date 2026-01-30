//package com.aivle.ai0917.ipai.domain.author.dashboard.repository;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.Work;
//import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface AuthorDashboardRepository extends JpaRepository<Work, String> {
//
//    long countByStatus(WorkStatus status);
//    List<Work> findAllByUserIntegrationId(String userIntegrationId);
//
//}
