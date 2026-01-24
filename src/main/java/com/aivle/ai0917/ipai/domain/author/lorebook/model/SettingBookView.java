// 2. 조회 전용 Immutable Entity
package com.aivle.ai0917.ipai.domain.author.lorebook.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Immutable
@Table(name = "v_setting_read")
public class SettingBookView {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "_varchar")
    private String[] userid;

    private String title;
    private String tag;
    private String keyword;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "_int4")
    private Integer[] episode;

    private String subtitle;

    @Column(columnDefinition = "json")
    private String settings;
}
