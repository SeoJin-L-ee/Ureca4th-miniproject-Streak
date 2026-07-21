package com.example.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{
	
	// 특정 회차에 연결된 과제 목록 조회 
	List<Assignment> findAllBySessionId(Long sessionId);
}
