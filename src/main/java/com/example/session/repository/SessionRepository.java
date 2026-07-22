package com.example.session.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long>{
	
	boolean existsByStudyIdAndSessionNumber(Long studyId, int sessionNumber);
	
	// 스터디 내에 있는 모든 회차를 조회 
	List<Session> findAllByStudyIdOrderBySessionNumberDesc(Long studyId);
	
	// 각 스터디 아이디별로, 현재 시각 기준 가장 가깝게 예정된 회차를 조회 (studyId, startsAt 오름차순으로 한번에)
	//	-> 서비스에서 각 스터디 아이디별 첫번째 값만 추릴 거임 (첫 번째 값이 가장 가깝게 예정된 회차)
	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.study
			WHERE s.study.id IN :studyIds
			  AND s.startsAt > :now
			ORDER BY s.study.id ASC, s.startsAt ASC
			""")
	List<Session> findNextSessionsByStudyIds(
			@Param("studyIds") List<Long> studyIds,
			@Param("now") LocalDateTime now);
}
