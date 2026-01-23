//package com.aivle.ai0917.ipai.domain.author.lorebook.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.Immutable;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.type.SqlTypes;
//import java.util.UUID;
//
//@Entity
//@Table(name = "setting")
//@Immutable // 읽기 전용 명시 (추가/수정/삭제 방지)
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class SettingBook {
//
//    @Id
//    private UUID id; // UUID 타입
//
//    @JdbcTypeCode(SqlTypes.ARRAY)
//    @Column(name = "writer", columnDefinition = "text[]", insertable = false, updatable = false)
//    private String[] writer;
//
//    @Column(nullable = false)
//    private String title;
//
//    private String tag;
//
//    @Column(name = "settings", columnDefinition = "json", insertable = false, updatable = false)
//    private String keyword;
//
//    @JdbcTypeCode(SqlTypes.ARRAY)
//    @Column(name = "episode", columnDefinition = "integer[]")
//    private Integer[] episode;
//
//    private String subtitle;
//
//    @JdbcTypeCode(SqlTypes.JSON) // PostgreSQL JSON 타입 매핑
//    @Column(name = "keyword", columnDefinition = "text", insertable = false, updatable = false)
//    private String settings;
//
//    // 벡터 데이터는 특수 처리가 필요하므로 단순 조회 시 무시하거나
//    // pgvector 라이브러리를 사용해야 합니다. 여기서는 정의만 유지합니다.
//    @Column(name = "embedding", columnDefinition = "vector", insertable = false, updatable = false)
//    private String embedding;
//
//}