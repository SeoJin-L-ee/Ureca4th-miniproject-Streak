package com.example.assignment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.assignment.converter.AssignmentConverter;
import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentDashboardDataDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.dto.response.AssignmentListResDto;
import com.example.assignment.dto.response.AssignmentRateComparisonDto;
import com.example.assignment.dto.response.AssignmentSubmissionRateDto;
import com.example.assignment.dto.response.NextSessionAssignmentDto;
import com.example.assignment.dto.response.SessionAssignmentCountDto;
import com.example.assignment.entity.Assignment;
import com.example.assignment.exception.AssignmentErrorCode;
import com.example.assignment.repository.AssignmentRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.exception.code.SessionErrorCode;
import com.example.session.repository.SessionRepository;
import com.example.submission.dto.response.SessionSubmissionCountDto;
import com.example.submission.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
	
	private final AssignmentRepository assignmentRepository;
	private final ParticipantRepository participantRepository;
	private final SessionRepository sessionRepository;
	private final SubmissionRepository submissionRepository;
	
	// 스터디장만 과제 생성 가능 
	@Override
	@Transactional
	public AssignmentInfoResDto createAssignment(Long studyId, Long sessionId, Long memberId, CreateAssignmentReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}

		// 해당 스터디에 속한 회차 존재 여부 검증 
		Session session = sessionRepository.findByIdAndStudyId(sessionId, studyId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.NOT_STUDY_SESSION));
		
		Assignment assignment = AssignmentConverter.toAssignment(session, reqDto);
		Assignment savedAssignment = assignmentRepository.save(assignment);
		
		return AssignmentConverter.toAssignmentInfoResDto(savedAssignment);
	}

	// 스터디장만 과제 수정 가능 
	@Override
	@Transactional
	public AssignmentInfoResDto updateAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId, UpdateAssignmentReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
		
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		assignment.updateAssignment(reqDto);
		
		return AssignmentConverter.toAssignmentInfoResDto(assignment);
	}

	
	// 스터디장만 과제 삭제 가능 
	@Override
	@Transactional
	public void deleteAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
				
		// 회차가 존재하고, 그 회차에 속해있는 과제인지 검증 
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		assignmentRepository.delete(assignment);
	}

	// 과제 상세 조회 - 해당 스터디 참여자만 조회 가능 
	@Override
	@Transactional(readOnly = true)
	public AssignmentInfoResDto detailAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId) {
		
		// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
		if (!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
			
		// 과제 존재 및 해당 회차 소속 여부 검증 
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		return AssignmentConverter.toAssignmentInfoResDto(assignment);
	}

	// 과제 목록 조회 - 해당 스터디 참여자만 조회 가능 
	@Override
	@Transactional(readOnly = true)
	public AssignmentListResDto listAssignment(Long studyId, Long sessionId, Long memberId) {
		
		// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
		if (!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}

		// 해당 회차가 해당 스터디 소속인지 검증
		if (!sessionRepository.existsByIdAndStudyId(sessionId, studyId))
			throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
		
		List<Assignment> assignments = assignmentRepository.findAllBySessionId(sessionId);

		return AssignmentConverter.toAssignmentListResDto(sessionId, assignments);
	}

	// 과제 제출률 비교 그래프 데이터 & 다음 회차의 과제들 & 회차별 과제 제출률 한번에 조회
	@Override
	@Transactional(readOnly = true)
	public AssignmentDashboardDataDto findAssignmentDashboardData(
			Long memberId, Long studyId, long currentParticipantCnt, Long nextSessionId,
			List<Long> sessionIds, LocalDateTime now
	) {
		// 과제 제출률 비교 그래프 데이터
		AssignmentRateComparisonDto comparisonDto = 
				calculateAssignmentRateComparison(memberId, studyId, currentParticipantCnt, now);
		// 다음 회차의 과제들
		List<NextSessionAssignmentDto> nextSessinAssignments =
				findNextSessionAssignments(memberId, nextSessionId, now);
		// 회차별 과제 제출률
		Map<Long, AssignmentSubmissionRateDto> teamSubmissionRates =
				calculateAssignmentRates(currentParticipantCnt, sessionIds);
		
		return new AssignmentDashboardDataDto(comparisonDto, nextSessinAssignments, teamSubmissionRates);
	}
	
	// 과제 제출률 비교 그래프 데이터
	private AssignmentRateComparisonDto calculateAssignmentRateComparison(
			Long memberId, Long studyId, long currentParticipantCnt, LocalDateTime now
	) {
		// 마감 기한이 지난 과제 개수 카운트 (없으면 null Dto 반환)
		long closedAssignmentsCnt = assignmentRepository.countClosedAssignments(studyId, now);
		if (closedAssignmentsCnt==0L) return new AssignmentRateComparisonDto(null, null);
		
		// 마감된 과제에 대한 전체 제출 건수 카운트
		long totalSubmissionCnt = submissionRepository.countStudySubmissionForClosed(studyId, now);
		
		// 마감된 과제에 대한 특정 멤버의 제출 건수 카운트
		long memberSubmissionCnt = submissionRepository.countMemberSubmissionForClosed(memberId, studyId, now);
		
		// 제출률 계산
		Double totalRate = caculateRate(totalSubmissionCnt, closedAssignmentsCnt*currentParticipantCnt);
		Double myRate = caculateRate(memberSubmissionCnt, closedAssignmentsCnt);
		
		return new AssignmentRateComparisonDto(totalRate, myRate);
	}
	
	// 다음 회차의 과제들
	private List<NextSessionAssignmentDto> findNextSessionAssignments(
			Long memberId, Long nextSessionId, LocalDateTime now
	) {
		if (nextSessionId == null) return List.of();
		// nextSessionId 에 해당하는 회차의 과제들 전부 조회
		List<Assignment> assignments = assignmentRepository.findAllBySessionIdOrderByDueAtAsc(nextSessionId);
		if (assignments.isEmpty()) return List.of();
		
		List<Long> assignmentIds = assignments.stream()
				.map(Assignment::getId).toList();
		
		// 다음 회차의 과제 id 중, 현재 멤버가 제출한 과제의 id
		Set<Long> submittedAssignmentIds = 
				new HashSet<>(submissionRepository.findSubmittedAssignmentIds(memberId, assignmentIds));
		
		return assignments.stream()
				.map(assignment -> {
					// 디데이 숫자 계산
					Long daysUntilDue = null;
					if (assignment.getDueAt() != null) {
						daysUntilDue = ChronoUnit.DAYS.between(now.toLocalDate(), assignment.getDueAt().toLocalDate());
					}
					// 제출 여부 확인
					boolean isSubmitted = submittedAssignmentIds.contains(assignment.getId());
					return new NextSessionAssignmentDto(
                            assignment.getId(),
                            assignment.getTitle(),
                            assignment.getDueAt(),
                            daysUntilDue,
                            isSubmitted);
                }).toList();
	}
	
	// 회차별 과제 제출률
	private Map<Long, AssignmentSubmissionRateDto> calculateAssignmentRates(
			long currentParticipantCnt, List<Long> sessionIds
	) {
		if (sessionIds.isEmpty()) return Map.of();
		// 회차별 과제 수 조회
		Map<Long, Long> assignmentCntBySessionId = assignmentRepository.countAssignmentsBySessionIds(sessionIds)
				.stream()
				.collect(Collectors.toMap(SessionAssignmentCountDto::sessionId, SessionAssignmentCountDto::assignmentCount));
		
		// 회차별 과제 제출 수 조회
		Map<Long, Long> submissionCntBySessionId = submissionRepository.countSubmissionsBySessionIds(sessionIds)
				.stream()
				.collect(Collectors.toMap(SessionSubmissionCountDto::sessionId, SessionSubmissionCountDto::submissionCount));
		
		return sessionIds.stream()
				.collect(Collectors.toMap(
						sessionId -> sessionId,
						sessionId -> {
							long assignmentCnt = assignmentCntBySessionId.getOrDefault(sessionId, 0L);
							if (assignmentCnt == 0L) return new AssignmentSubmissionRateDto(false, null);
							
							long submissionCnt = submissionCntBySessionId.getOrDefault(sessionId, 0L);
							// 회차별 제출률 계산
							Double rate = caculateRate(submissionCnt, assignmentCnt*currentParticipantCnt);
							return new AssignmentSubmissionRateDto(true, rate);
						}));
	}
	
	// 제출률 계산 (소수점 첫째자리까지 반올림)
	private Double caculateRate(long a, long b) {
		if (b == 0L) return null;
		double rate = (double) a/b * 100.0;
		
		return BigDecimal.valueOf(rate)
						 .setScale(1, RoundingMode.HALF_UP)
						 .doubleValue();
	}
	
}
