package com.aivle.ai0917.ipai.domain.test.repository;

import com.aivle.ai0917.ipai.domain.test.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// JpaRepository를 상속받는 것만으로 기본적인 저장/조회 기능이 완성됩니다.
public interface TestRepository extends JpaRepository<Test, Long> {
}