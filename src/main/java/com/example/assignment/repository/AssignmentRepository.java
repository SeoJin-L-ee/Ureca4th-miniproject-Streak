package com.example.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{
	
	// 특정 회차에 연결된 과제 목록 조회 
	List<Assignment> findAllBySessionId(Long sessionId);
	
	// 과제 ID와 회차 ID가 일치하는 과제 조회 
	Optional<Assignment> findByIdAndSessionId(Long assignmentId, Long sessionId);
}
