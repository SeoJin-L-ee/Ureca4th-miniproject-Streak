package com.example.study.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.participant.entity.Participant;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.converter.StudyConverter;
import com.example.study.dto.response.StudyAttendanceRateDto;
import com.example.study.dto.response.StudySummaryListResDto;
import com.example.study.dto.response.StudySummaryResDto;
import com.example.study.entity.enums.StudyStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {
	
	private final SessionRepository sessionRepository;
	private final ParticipantRepository participantRepository;
	private final AttendanceRepository attendanceRepository;

	@Override
	// 사용자별 참여 중인 스터디 목록 조회
	public StudySummaryListResDto findStudySummaryList(Long memberId, Pageable pageable) {
		
		// 해당 사용자의 스터디 참여내역을 모두 조회 (종료되거나 삭제되지 않은, RECRUITING나 CLOSED인 스터디의 참여내역만)
		Page<Participant> participantPage = participantRepository.findParticipantsByMemberId(memberId, StudyStatus.ENDED, pageable);
		List<Participant> participants = participantPage.getContent();
		// 가져온 데이터가 비어있으면 빈 Dto 반환
		if (participants.isEmpty()) return StudyConverter.toStudySummaryListResDto(participantPage, List.of());
		
		// 조회한 스터디의 id 모으기
		List<Long> studyIds = participants.stream().map(p -> p.getStudy().getId()).toList();
		
		// 각 스터디별 다음 회차 구하기
		Map<Long, Session> nextSessions = getNextSessionInfos(studyIds, LocalDateTime.now());
		// 각 스터디별 전체 참석률 구하기
		Map<Long, Double> attendanceRates = getAttendanceRates(studyIds, LocalDateTime.now());
		
		// participants 와 구한 데이터를 합쳐서 StudySummaryResDto 생성
		List<StudySummaryResDto> studySummaryResDtos = StudyConverter.toStudySummaryResDtoList(
				participants, nextSessions, attendanceRates);
		// 페이지 정보까지 합쳐서 최종 DtoList 반환
		return StudyConverter.toStudySummaryListResDto(participantPage, studySummaryResDtos);
	}
	
	// 각 스터디별 다음 회차 구하기
	private Map<Long, Session> getNextSessionInfos(List<Long> studyIds, LocalDateTime now) {
		// 각 스터디 아이디별로, 현재 시각 기준 가장 가깝게 예정된 회차를 조회
		List<Session> nextSessions = sessionRepository.findNextSessionsByStudyIds(studyIds, now);
		
		return nextSessions.stream()
				.collect(Collectors.toMap(
						s -> s.getStudy().getId(),
						s -> s,
						(existing, replacement) -> existing)); // 이미 값이 있으면 기존 값 유지하게 할 수 있게 해준다고 함 (먼저 조회된 값이 더 가까운 회차 )
	}
	
	// 각 스터디별 전체 참석률 구하기
	private Map<Long, Double> getAttendanceRates(List<Long> studyIds, LocalDateTime now) {
		return attendanceRepository.calculateStudyAttendanceRatesByStudyIds(studyIds, now, AttendanceStatus.PRESENT).stream()
				.collect(Collectors.toMap(StudyAttendanceRateDto::studyId, StudyAttendanceRateDto::attendanceRate));
	}

}
