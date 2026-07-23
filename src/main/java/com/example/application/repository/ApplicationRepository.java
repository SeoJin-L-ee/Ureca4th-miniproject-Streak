package com.example.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
	
	//마이페이지- 스터디 지원 현황 조회용 - 상태 필터 없이 전체 (study를 함께 fetch해서 N+1 방지)
	@Query("SELECT a FROM Application a JOIN FETCH a.study WHERE a.applicant.id = :memberId")
	List<Application> findAllByApplicantId(@Param("memberId") Long memberId);

	//마이페이지 - 스터디 지원 현황 조회용 - '?status=' 로 필터링할 때
	@Query("SELECT a FROM Application a JOIN FETCH a.study WHERE a.applicant.id = :memberId AND a.status = :status")
	List<Application> findAllByApplicantIdAndStatus(@Param("memberId") Long memberId, @Param("status") ApplicationStatus status);
}
