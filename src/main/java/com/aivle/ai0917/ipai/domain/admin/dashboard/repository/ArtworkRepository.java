//package com.aivle.ai0917.ipai.domain.admin.dashboard.repository;
//
//import com.aivle.ai0917.ipai.domain.author.works.model.Work;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface ArtworkRepository extends JpaRepository<Work, Long> {
//
//    // 저장된 작품 수
//    @Query("SELECT COUNT(w) FROM Work w")
//    Long countSaveArtworks();
//}