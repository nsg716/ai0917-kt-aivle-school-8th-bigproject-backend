package com.aivle.ai0917.ipai.domain.test.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_table")
@Getter
@Setter
@NoArgsConstructor  // JPA용 필수 기본 생성자
@AllArgsConstructor // 모든 필드 생성자 (편의용)
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @CreationTimestamp // 데이터 저장 시 자동으로 현재 시간 입력
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}