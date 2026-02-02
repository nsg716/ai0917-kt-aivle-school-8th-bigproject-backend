package com.aivle.ai0917.ipai.domain.author.works.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "active_works_view")
@Getter
public class Work {

    @Id
    private Long id;

    @Column(name = "universe_id")
    private Long universeId;

    @Column(name = "primary_author_id")
    private String primaryAuthorId;

    private String title;
    private String synopsis;
    private String genre;

    @Enumerated(EnumType.STRING) // [수정] Enum 매핑 추가
    private WorkStatus status;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}