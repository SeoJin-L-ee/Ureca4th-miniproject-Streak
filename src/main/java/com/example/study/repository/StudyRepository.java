package com.example.study.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.study.entity.Study;

import jakarta.persistence.LockModeType;

public interface StudyRepository extends JpaRepository<Study, Long> {

	// id 기반으로 삭제되지 않은 Study 검색
	Optional<Study> findByIdAndIsDeletedFalse(Long id);
	
	// 정원 초과될 수도 있으니 얘도 비관적 락
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT s FROM Study s
			WHERE s.id = :studyId
				AND s.isDeleted = false
			""")
	Optional<Study> findByIdForUpdate(@Param("studyId") Long studyId);
	
}
