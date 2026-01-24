// ManuscriptView.java
package com.aivle.ai0917.ipai.domain.author.manuscript.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "author_manuscript_view")
@Getter
public class ManuscriptView {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id")
    private String userId;

    private String title;
    private Integer episode;
    private String subtitle;
    private String txt;
}
