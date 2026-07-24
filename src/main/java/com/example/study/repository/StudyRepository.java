package com.example.study.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;

import jakarta.persistence.LockModeType;

public interface StudyRepository extends JpaRepository<Study, Long> {

	// id 기반으로 삭제되지 않은 Study 검색
	Optional<Study> findByIdAndIsDeletedFalse(Long id);
	
	// id 기반으로 존재하면서 삭제되지 않은 스터디인지만 검증
	boolean existsByIdAndIsDeletedFalse(Long studyId);
	
	// 정원 초과될 수도 있으니 얘도 비관적 락
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT s FROM Study s
			WHERE s.id = :studyId
				AND s.isDeleted = false
			""")
	Optional<Study> findByIdForUpdate(@Param("studyId") Long studyId);
	
	// 카테고리와 제목 (선택) 조건으로 삭제되지 않은 스터디 페이징 조회
	@Query("""
			SELECT s FROM Study s
			WHERE s.isDeleted = false
				AND (:category IS NULL OR s.category = :category)
				AND (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%')))
			""")
	Page<Study> findAllForApply(
			@Param("category") StudyCategory category,
			@Param("title") String title,
			Pageable pageable);
}
