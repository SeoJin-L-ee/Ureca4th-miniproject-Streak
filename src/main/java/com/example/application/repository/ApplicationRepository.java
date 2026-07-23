package com.example.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	// 대기 지원자 수 카운트 (넘어오는 ApplicationStatus는 PENDING임)
	long countByStudyIdAndStatus(Long studyId, ApplicationStatus status);
}
