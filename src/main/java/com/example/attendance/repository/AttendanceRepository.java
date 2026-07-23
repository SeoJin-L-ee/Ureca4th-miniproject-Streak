package com.example.attendance.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.attendance.dto.response.MemberSessionAttendanceDto;
import com.example.attendance.dto.response.SessionAttendanceRateDto;
import com.example.attendance.dto.response.StudyAttendanceRateDto;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
	
	// 특정 회차의 출석 목록 조회 
	List<Attendance> findAllBySessionId(Long sessionId);

	// Member 별 스터디 참석/미참석 횟수 count
	int countBySessionStudyIdAndMemberIdAndStatus(Long studyId, Long memberId, AttendanceStatus status);

	// 여러 스터디의 지나간 회차들에 대한 출석률을 GROUP BY로 집계 (Dto 하나 새로 만들어서 거기다가 프로젝션함)
    // 출석(PRESENT)인 경우에만 카운트해서, 전체 회차 수로 나눔
	@Query("""
			SELECT new com.example.attendance.dto.response.StudyAttendanceRateDto(
				s.study.id AS studyId,
				ROUND(( SUM(CASE WHEN a.status = :presentStatus THEN 1.0 ELSE 0.0 END) / COUNT(a) )*100, 1) AS attendanceRate
			)
			FROM Attendance a
			JOIN a.session s
			WHERE s.study.id IN :studyIds
			  AND s.startsAt < :now
			GROUP BY s.study.id
			""")
	List<StudyAttendanceRateDto> calculateStudyAttendanceRatesByStudyIds(
			@Param("studyIds") List<Long> studyIds,
			@Param("now") LocalDateTime now,
			@Param("presentStatus") AttendanceStatus presentStatus
	);
	
	//마이페이지 - 최장 Streak 계산용. 회차 시작 시간(session.startsAt) 오름차순 정렬
	@Query("""
			SELECT a FROM Attendance a
			JOIN FETCH a.session
			WHERE a.member.id = :memberId
			ORDER BY a.session.startsAt asc
			""")
	List<Attendance> findAllByMemberIdOrderBySessionStartsAtAsc(@Param("memberId") Long memberId);

	//마이페이지 - 평균 출석률 계산용. 전체 출석 대상 회차 수
	long countByMemberId(Long memberId);

	//마이페이지 - 평균 출석률 계산용. 그중 특정 상태(PRESENT)로 카운트된 횟수
	long countByMemberIdAndStatus(Long memberId, AttendanceStatus status);

	// 스터디 전체 평균 출석률 (대시보드 상단 그래프에 사용됨)
	@Query("""
			SELECT COALESCE(
					ROUND(( SUM(CASE WHEN a.status = :presentStatus THEN 1.0 ELSE 0.0 END) / COUNT(a) )*100, 1),
					0.0)
			FROM Attendance a
			JOIN a.session s
			WHERE s.study.id = :studyId
				AND s.startsAt < :now
			""")
	Double calculateStudyAttendanceRateByStudyId(
			@Param("studyId") Long studyId,
			@Param("now") LocalDateTime now,
			@Param("presentStatus") AttendanceStatus presentStatus
	);
	
	// 스터디의 내 평균 출석률 (대시보드 상단 그래프에 사용됨)
	@Query("""
			SELECT COALESCE(
					ROUND(( SUM(CASE WHEN a.status = :presentStatus THEN 1.0 ELSE 0.0 END) / COUNT(a) )*100, 1),
					0.0)
			FROM Attendance a
			JOIN a.session s
			WHERE s.study.id = :studyId
				AND s.startsAt < :now
				AND a.member.id = :memberId
			""")
	Double calculateMemberAttendanceRate(
			@Param("studyId") Long studyId,
			@Param("memberId") Long memberId,
			@Param("now") LocalDateTime now,
			@Param("presentStatus") AttendanceStatus presentStatus
	);
	
	// 특정 스터디의 회차별 평균 출석률 (회차별로 group by)
	@Query("""
			SELECT new com.example.attendance.dto.response.SessionAttendanceRateDto(
				a.session.id,
				COALESCE(
					ROUND(( SUM(CASE WHEN a.status = :presentStatus THEN 1.0 ELSE 0.0 END) / COUNT(a) )*100, 1),
					0.0))
			FROM Attendance a
			WHERE a.session.id IN :sessionIds
				AND a.session.startsAt < :now
			GROUP BY a.session.id
			""")
	List<SessionAttendanceRateDto> calculateSessionAttendanceRatesBySessionIds(
			@Param("sessionIds") List<Long> sessionIds,
			@Param("now") LocalDateTime now,
			@Param("presentStatus") AttendanceStatus presentStatus
	);
	
	// 특정 member 의 회차별 출석 상태 조회
	@Query("""
			SELECT new com.example.attendance.dto.response.MemberSessionAttendanceDto(
				a.session.id,
				a.status)
			FROM Attendance a
			WHERE a.member.id = :memberId
				AND a.session.id IN :sessionIds
			""")
	List<MemberSessionAttendanceDto> findSessionAttendanceStatusesByMemberId(
			@Param("memberId") Long memberId,
			@Param("sessionIds") List<Long> sessionIds
	);
	
}
