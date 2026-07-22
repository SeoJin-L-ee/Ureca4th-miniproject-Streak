package com.example.session.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long>{
	
	boolean existsByStudyIdAndSessionNumber(Long studyId, int sessionNumber);
	
	// 스터디 내에 있는 모든 회차를 조회 
	List<Session> findAllByStudyIdOrderBySessionNumberDesc(Long studyId);
	
	// 스터디 내에 있는 모든 회차의 개수를 조회 
	int countByStudyId(Long studyId);
}
