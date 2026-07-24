package com.example.study.service;

import com.example.member.entity.Member;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.dto.response.MemberApplicationStatusDto;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.repository.ApplicationRepository;
import com.example.assignment.dto.response.AssignmentDashboardDataDto;
import com.example.assignment.service.AssignmentService;
import com.example.attendance.dto.response.AttendanceDashboardDataDto;
import com.example.attendance.dto.response.StudyAttendanceRateDto;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.dto.response.SessionDashboardDataDto;
import com.example.session.dto.response.SessionSummaryResDto;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.session.service.SessionService;
import com.example.study.converter.StudyConverter;
import com.example.study.converter.StudyDashboardConverter;
import com.example.study.dto.response.StudyApplyDetailResDto;
import com.example.study.dto.response.StudyApplySummaryListResDto;
import com.example.study.dto.response.StudyApplySummaryResDto;
import com.example.study.dto.response.StudyDashboardResDto;
import com.example.study.dto.response.StudyLeaderDto;
import com.example.study.dto.response.StudyParticipantCountDto;
import com.example.study.dto.response.StudySummaryListResDto;
import com.example.study.dto.response.StudySummaryResDto;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.exception.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {
	
	private final MemberRepository memberRepository;
	private final StudyRepository studyRepository;
	private final SessionRepository sessionRepository;
	private final ApplicationRepository applicationRepository;
	private final ParticipantRepository participantRepository;
	private final AttendanceRepository attendanceRepository;
	
	private final SessionService sessionService;
	private final AttendanceService attendanceService;
	private final AssignmentService assignmentService;

	@Override
	@Transactional(readOnly = true)
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
	
	@Override
	@Transactional(readOnly = true)
	// 특정 스터디 상세 조회 (대시보드)
	public StudyDashboardResDto findStudyDashboard(Long memberId, Long studyId, Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		Study study = studyRepository.findByIdAndIsDeletedFalse(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		Participant participant = participantRepository.findByStudyIdAndMemberId(studyId, memberId)
				.orElseThrow(() -> new GeneralException(CommonErrorCode.FORBIDDEN));
		
		// 현재 참여 인원
		long currentParticipantCnt = participantRepository.countByStudyId(studyId);
		
		// 대기 지원자 수 (스터디장만 볼 수 있음)
		boolean isLeader = participant.getRole() == StudyRole.LEADER;
		long currentApplicationCnt = isLeader ? applicationRepository.countByStudyIdAndStatus(studyId, ApplicationStatus.PENDING) : 0L;
		
		// 가까운 다음 회차 & 전체 회차 목록 한번에 조회
		SessionDashboardDataDto sessionDatas = sessionService.findSessionDashboardData(studyId, now, pageable);
		
		// 이후 조회에 필요한 아이디들 뽑아오기
		List<Long> sessionIds = sessionDatas.sessions().getContent().stream()
									.map(SessionSummaryResDto::sessionId)
									.toList();
		Long nextSessionId = sessionDatas.nextSession() == null ? null : sessionDatas.nextSession().sessionId();
		
		// 출석률 비교 그래프 데이터 & 회차별 전체 출석률 & 회차별 내 출석 여부 한번에 조회
		AttendanceDashboardDataDto attendanceDatas = attendanceService.findAttendanceDashboardData(memberId, studyId, sessionIds, now);
		
		// 과제 제출률 비교 그래프 데이터 & 다음 회차의 과제들 & 회차별 과제 제출률 한번에 조회
		AssignmentDashboardDataDto assignmentDatas = assignmentService.findAssignmentDashboardData(
				memberId,
				studyId,
				currentParticipantCnt,
				nextSessionId,
				sessionIds,
				now);
		
		// 위에서 얻은 데이터들 한번에 묶어서 반환
		return StudyDashboardConverter.toStudyDashboardResDto(
				study,
				isLeader,
				currentApplicationCnt,
				currentParticipantCnt,
				sessionDatas,
				attendanceDatas,
				assignmentDatas,
				now);
	}
	
	@Override
	@Transactional(readOnly = true)
	// 스터디 탐색 목록 조회 (전체, 카테고리별, 제목 검색)
	public StudyApplySummaryListResDto getStudiesForApply(Long memberId, StudyCategory category, String title, Pageable pageable) {
		if (!memberRepository.existsById(memberId)) throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
		
		Page<Study> studyPage = studyRepository.findAllForApply(category, title, pageable);
		List<Study> studies = studyPage.getContent();
		if (studies.isEmpty()) {
			return new StudyApplySummaryListResDto(
					studyPage.getNumber(),
					studyPage.getSize(),
					studyPage.getTotalPages(),
					studyPage.getTotalElements(),
					studyPage.hasNext(),
					Collections.emptyList());
		}
		List<Long> studyIds = studies.stream().map(Study::getId).toList();
		
		// 각 스터디별 스터디장 이름 한번에 가져오기
		Map<Long, String> leaderMap = participantRepository.findLeadersByStudyIds(studyIds, StudyRole.LEADER).stream()
	            .collect(Collectors.toMap(
	            		StudyLeaderDto::studyId,
	            		StudyLeaderDto::leaderName));
		
		// 각 스터디별 참여자 수 한번에 가져오기
		Map<Long, Integer> participantCountMap = participantRepository.countParticipantsByStudyIds(studyIds).stream()
				.collect(Collectors.
						toMap(StudyParticipantCountDto::studyId,
						p -> p.count().intValue()));
		
		// 각 스터디에 대한 지원 상태 (이미 지원함, 참여중, 지원가능 등) 한번에 가져오기
		Map<Long, StudyApplySummaryResDto.MyStudyStatus> myStudyStatuses = 
				applicationRepository.findMyApplicationStatuses(memberId, studyIds).stream()
					.collect(Collectors.toMap(
							MemberApplicationStatusDto::studyId,
							p -> changeToMyStudyStatus(p.status())
					));
		
		// 합치기
		List<StudyApplySummaryResDto> summaryDtos = studies.stream()
				.map(study -> new StudyApplySummaryResDto(
						study.getId(),
						study.getTitle(),
						leaderMap.getOrDefault(study.getId(), "알 수 없음"),
						study.getCategory(),
						study.getStatus(),
						myStudyStatuses.getOrDefault(study.getId(), StudyApplySummaryResDto.MyStudyStatus.NONE),
						participantCountMap.getOrDefault(study.getId(), 0),
						study.getCapacity(),
						study.getCreatedAt()
				)).toList();
		
		return new StudyApplySummaryListResDto(
				studyPage.getNumber(),
				studyPage.getSize(),
				studyPage.getTotalPages(),
				studyPage.getTotalElements(),
				studyPage.hasNext(),
				summaryDtos);
	}

	@Override
	@Transactional(readOnly = true)
	// 스터디 상세 조회 (스터디 찾기 화면에서)
	public StudyApplyDetailResDto getStudyDetailForApply(Long memberId, Long studyId) {
		if (!memberRepository.existsById(memberId)) throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
		Study study = studyRepository.findByIdAndIsDeletedFalse(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		Participant leaderParticipant = participantRepository.findLeaderByStudyId(studyId, StudyRole.LEADER)
				.orElseThrow(() -> new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND));
		Member leader = leaderParticipant.getMember();
		
		// 현재 참여자 수 계산
		int currentParticipantCnt = (int) participantRepository.countByStudyId(studyId);

		StudyApplySummaryResDto.MyStudyStatus myStatus = StudyApplySummaryResDto.MyStudyStatus.NONE;
        boolean isLeader = false;
        // 스터디장 여부 확인
    	isLeader = leader.getId().equals(memberId);
        if (isLeader) {
        	// 스터디장이면 굳이 조회 없이 ACCEPTED 처리
            myStatus = StudyApplySummaryResDto.MyStudyStatus.ACCEPTED;
        } else {
        	// 일반 멤버면 가장 최근 지원서 확인해서 상태 결정
            myStatus = applicationRepository.findTopByStudyIdAndApplicantIdOrderByCreatedAtDesc(studyId, memberId)
                    .map(app -> changeToMyStudyStatus(app.getStatus()))
                    .orElse(StudyApplySummaryResDto.MyStudyStatus.NONE);
        }
        return new StudyApplyDetailResDto(
                study.getId(),
                study.getTitle(),
                leader.getId(),
                leader.getName(),
                study.getDescription(),
                study.getCategory(),
                study.getStatus(),
                myStatus,
                currentParticipantCnt,
                study.getCapacity(),
                isLeader,
                study.getCreatedAt());
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
	
	// MyStudyStatus 타입으로 바꿔줌
	private StudyApplySummaryResDto.MyStudyStatus changeToMyStudyStatus(ApplicationStatus status) {
		if (status == null) return StudyApplySummaryResDto.MyStudyStatus.NONE;
		return switch (status) {
			case PENDING -> StudyApplySummaryResDto.MyStudyStatus.PENDING;
			case APPROVED -> StudyApplySummaryResDto.MyStudyStatus.ACCEPTED;
			case REJECTED -> StudyApplySummaryResDto.MyStudyStatus.REJECTED;};
	}

}
