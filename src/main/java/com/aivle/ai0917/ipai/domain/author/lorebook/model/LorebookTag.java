// LorebookTag.java
package com.aivle.ai0917.ipai.domain.author.lorebook.model;

import lombok.Getter;

@Getter
public enum LorebookTag {
    CHARACTERS("characters", "캐릭터"),
    WORLDVIEW("worldview", "세계관"),
    NARRATIVE("narrative", "사건");

    private final String path;
    private final String description;

    LorebookTag(String path, String description) {
        this.path = path;
        this.description = description;
    }

    public static LorebookTag fromPath(String path) {
        for (LorebookTag tag : values()) {
            if (tag.path.equalsIgnoreCase(path)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Invalid tag: " + path);
    }
}
