package com.example.study.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {

	// id 기반으로 삭제되지 않은 Study 검색
	Optional<Study> findByIdAndIsDeletedFalse(Long id);
}
