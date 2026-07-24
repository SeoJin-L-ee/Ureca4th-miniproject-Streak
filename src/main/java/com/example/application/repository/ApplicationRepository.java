package com.example.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.application.dto.response.MemberApplicationStatusDto;
import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;

import jakarta.persistence.LockModeType;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
	
	//마이페이지- 스터디 지원 현황 조회용 - 상태 필터 없이 전체 (study를 함께 fetch해서 N+1 방지)
	@Query("""
			SELECT a FROM Application a
			JOIN FETCH a.study
			WHERE a.applicant.id = :memberId
			""")
	List<Application> findAllByApplicantId(@Param("memberId") Long memberId);
	
	//마이페이지 - 스터디 지원 현황 조회용 - '?status=' 로 필터링할 때
	@Query("""
			SELECT a FROM Application a
			JOIN FETCH a.study
			WHERE a.applicant.id = :memberId
			AND a.status = :status
			""")
	List<Application> findAllByApplicantIdAndStatus(@Param("memberId") Long memberId, @Param("status") ApplicationStatus status);
	
	// 대기 지원자 수 카운트 (넘어오는 ApplicationStatus는 PENDING임)
	long countByStudyIdAndStatus(Long studyId, ApplicationStatus status);
	
	// memberId 와 studyId 사이의 지원 관계를 조회
	Optional<Application> findByApplicantIdAndStudyId(Long applicantId, Long studyId);
	
	// 상태 업데이트시키기 위해 지원 객체 조회
	// 이미 처리된 지원에 대해서 에러 발생시키고, 정원 초과도 검증해야하므로 동시성 이슈를 고려해야 함
	//	-> PESSIMISTIC_WRITE으로 비관적 락 걸어둠
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT a
			FROM Application a
				JOIN FETCH a.applicant
				JOIN FETCH a.study
			WHERE a.id = :applicationId
			""")
	Optional<Application> findByIdForStatusUpdate(@Param("applicationId") Long applicationId);
	
	// 삭제를 위해 지원 객체 조회 (요청한 member 의 지원만 조회)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT a
			FROM Application a
				JOIN FETCH a.applicant
				JOIN FETCH a.study
			WHERE a.id = :applicationId
				AND a.applicant.id = :applicantId
			""")
	Optional<Application> findMineByIdForStatusUpdate(
			@Param("applicationId") Long applicationId,
			@Param("applicantId") Long applicantId
	);
	
	// 스터디 목록 조회를 위해, 현재 사용자의 지원 상태를 전부 조회
	@Query("""
			SELECT new com.example.application.dto.response.MemberApplicationStatusDto(
				a.study.id, 
				a.status)
			FROM Application a
			WHERE a.applicant.id = :applicantId
				AND a.study.id IN :studyIds
			""")
	List<MemberApplicationStatusDto> findMyApplicationStatuses(
			@Param("applicantId") Long applicantId,
			@Param("studyIds") List<Long> studyIds
	);
	
	// 특정 스터디의 모든 지원 내역 조회
	@Query("""
			SELECT a
			FROM Application a
				JOIN FETCH a.applicant
			WHERE a.study.id = :studyId
			ORDER BY a.createdAt DESC
			""")
	List<Application> findAllByStudyIdWithApplicant(@Param("studyId") Long studyId);
	
	// 스터디 지원 상태 조회 (가장 최근 지원 내역)
    Optional<Application> findTopByStudyIdAndApplicantIdOrderByCreatedAtDesc(Long studyId, Long applicantId);
	
}
