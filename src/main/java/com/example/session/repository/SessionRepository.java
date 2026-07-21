package com.example.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long>{
	
	boolean existsByStudyIdAndSessionNumber(Long studyId, int sessionNumber);
}
