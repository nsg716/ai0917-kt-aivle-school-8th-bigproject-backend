package com.aivle.ai0917.ipai.domain.author.works.model;

import lombok.Getter;

@Getter
public enum WorkStatus {
    NEW("신규"),
    ONGOING("연재중"),
    COMPLETED("완결"),
    DELETED("삭제됨");

    private final String description;

    WorkStatus(String description) {
        this.description = description;
    }
}