package com.example.session.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long>{
	
	boolean existsByStudyIdAndSessionNumber(Long studyId, int sessionNumber);
	
	// 스터디 내에 있는 모든 회차를 조회 
	List<Session> findAllByStudyIdOrderBySessionNumberDesc(Long studyId);

	// 스터디 내에 있는 모든 회차의 개수를 조회
	int countByStudyId(Long studyId);

	// 해당 스터디에 존재하는 회차인지 검증
	boolean existsByIdAndStudyId(Long sessionId, Long studyId);

	// 해당 스터디에 존재하는 회차 가져오기
	Optional<Session> findByIdAndStudyId(Long sessionId, Long studyId);

	// 특정 기간동안 멤버의 회차 조회
	@Query("""
			SELECT DISTINCT s
			FROM Session s
			JOIN FETCH s.study st
			JOIN Participant p ON p.study = st
			WHERE p.member.id = :memberId
				AND s.startsAt BETWEEN :start AND :end
	""")
	List<Session> findByMemberIdAndDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

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
	
	//마이페이지 - 오늘 회차 조회용. 내가 참여 중인 스터디들 중 시작 시간이 오늘 범위(start~end)인 회차만
	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.study
			WHERE s.study.id IN (SELECT p.study.id FROM Participant p WHERE p.member.id = :memberId)
			AND s.startsAt BETWEEN :start AND :end
			ORDER BY s.startsAt asc
			""")
	List<Session> findTodaySessionsByMemberId(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
