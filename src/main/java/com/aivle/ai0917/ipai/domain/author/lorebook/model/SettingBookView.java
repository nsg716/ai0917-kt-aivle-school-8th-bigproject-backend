package com.aivle.ai0917.ipai.domain.author.lorebook.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Immutable
@Table(name = "active_lorebooks_view")
public class SettingBookView {

    @Id
    private Long id;

    // [수정] DB의 varchar[] 타입에 맞춰 List<String>으로 변경 및 타입 매핑
    @Column(name = "user_id", columnDefinition = "varchar[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> userId;

    @Column(name = "work_id")
    private Long workId;

    private String category;

    private String keyword;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode setting;

    @Column(name = "ep_num", columnDefinition = "integer[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<Integer> epNum;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}