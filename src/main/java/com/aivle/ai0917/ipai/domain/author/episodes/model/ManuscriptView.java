package com.aivle.ai0917.ipai.domain.author.episodes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "active_episodes_view") // DB에 이 이름의 View가 있어야 합니다.
@Getter
public class ManuscriptView {

    @Id
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "work_id")
    private Long workId;

    private String title;

    // [수정] DB 컬럼명은 ep_num, 자바 필드명은 episode
    @Column(name = "ep_num")
    private Integer episode;

    private String subtitle;

    // [수정] DB 컬럼명은 txt_path, 자바 필드명은 txt
    @Column(name = "txt_path")
    private String txt;
}