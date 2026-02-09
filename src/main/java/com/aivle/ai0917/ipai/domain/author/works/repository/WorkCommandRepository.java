package com.aivle.ai0917.ipai.domain.author.works.repository;

import com.aivle.ai0917.ipai.domain.author.works.model.Work;
import com.aivle.ai0917.ipai.domain.author.works.model.WorkStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkCommandRepository extends Repository<Work, Long> {

    @Transactional
    @Query(value = """
        INSERT INTO works (universe_id, primary_author_id, title, synopsis, genre, cover_image_url, status)
        VALUES (:universeId, :authorId, :title, :synopsis, :genre, :coverImageUrl, :status)
        RETURNING id
        """, nativeQuery = true) // '연재중' 하드코딩 제거하고 파라미터로 받음
    Long insert(
            @Param("universeId") Long universeId,
            @Param("authorId") String authorId,
            @Param("title") String title,
            @Param("synopsis") String synopsis,
            @Param("genre") String genre,
            @Param("coverImageUrl") String coverImageUrl,
            @Param("status") String status // [수정] 추가됨
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE works SET status = :status WHERE id = :id", nativeQuery = true)
    int updateStatus(@Param("id") Long id, @Param("status") String status); // String으로 받아서 처리

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE works
        SET title = :title,
            synopsis = :synopsis,
            genre = :genre,
            cover_image_url = :coverImageUrl
        WHERE id = :id
        """, nativeQuery = true)
    int updateWork(
            @Param("id") Long id,
            @Param("title") String title,
            @Param("synopsis") String synopsis,
            @Param("genre") String genre,
            @Param("coverImageUrl") String coverImageUrl
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE works SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int deleteById(@Param("id") Long id); // 쿼리 내용을 Soft Delete로 변경


    // ipext 에서 사용
    /**
     * [추가] 매니저 ID로 담당 작가들의 작품 조회
     */
    @Query("""
    SELECT w
    FROM Work w
    WHERE w.primaryAuthorId = :managerId
      AND w.status <> :deletedStatus
    ORDER BY w.createdAt DESC
""")
    List<Work> findAllByManagerId(
            @Param("managerId") String managerId,
            @Param("deletedStatus") WorkStatus deletedStatus
    );



    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM works WHERE deleted_at <= :threshold", nativeQuery = true)
    long deleteByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
