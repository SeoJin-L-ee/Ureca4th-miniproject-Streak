package com.example.session.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.converter.SessionAssignmentConverter;
import com.example.session.converter.SessionAttendanceConverter;
import com.example.session.converter.SessionConverter;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionAssignmentResDto;
import com.example.session.dto.response.SessionAttendanceResDto;
import com.example.session.dto.response.SessionDashboardDataDto;
import com.example.session.dto.response.SessionInfoResDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.dto.response.SessionSummaryResDto;
import com.example.session.entity.Session;
import com.example.session.exception.code.SessionErrorCode;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.exception.code.StudyErrorCode;
import com.example.study.repository.StudyRepository;
import com.example.submission.repository.SubmissionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
	
	private final SessionRepository sessionRepository;
	private final StudyRepository studyRepository;
	private final ParticipantRepository participantRepository;
	private final AttendanceRepository attendanceRepository;
	private final AssignmentRepository assignmentRepository;
	private final SubmissionRepository submissionRepository;
	
	
	// 스터디 회차 생성 
	@Override
	@Transactional
	public SessionResDto createSession(long studyId, long memberId, CreateSessionReqDto reqDto) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
			
		Session session = SessionConverter.toSession(reqDto, study);
		Session savedSession = sessionRepository.save(session);
		
		return SessionConverter.toDetailResDto(savedSession);
	}

	
	// 스터디 회차 수정 
	@Override
	@Transactional
	public SessionResDto updateSession(long studyId, long sessionId, long memberId, UpdateSessionReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.SESSION_NOT_FOUND));
		
		if(reqDto.sessionNumber() != null && session.getSessionNumber() != reqDto.sessionNumber()) {
			if(sessionRepository.existsByStudyIdAndSessionNumber(studyId, reqDto.sessionNumber())) {
				throw new GeneralException(SessionErrorCode.DUPLICATE_SESSION_NUMBER);
			}
		}
		
		session.updateSession(reqDto);
		
		return SessionConverter.toDetailResDto(session);
	}

	
	// 스터디 회차 삭제 
	@Override
	@Transactional
	public void deleteSession(long studyId, long sessionId, long memberId) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		sessionRepository.deleteById(sessionId);
	}


	// 스터디 회차 상세 조회 
	@Override
	public SessionInfoResDto detailSession(long studyId, long sessionId, long memberId) {
		
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.SESSION_NOT_FOUND));
		
		// Session이 Study에 속한 건지에 대한 검증 
		if (!session.getStudy().getId().equals(studyId)) {
			throw new GeneralException(CommonErrorCode.BAD_REQUEST);
		}
		
		// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차의 전체 과제 목록 조회 
		List<Assignment> assignments =  assignmentRepository.findAllBySessionId(sessionId);
		
		// 로그인한 유저가 해당 회차에서 제출한 과제 ID 목록을 한 번에 조회하여 Set으로 저장 
		Set<Long> submittedAssignmentIds = new HashSet<>(
				submissionRepository.findSubmittedAssignmentIdsBySessionIdAndMemberId(sessionId, memberId)
		);
		
		List<SessionAssignmentResDto> assignmentResDtos = assignments.stream()
		        .map(assignment -> {
		            // 로그인한 유저의 과제 제출 여부 확인
		            boolean isSubmitted = submittedAssignmentIds.contains(assignment.getId());
		            
		            // 2개의 인자를 모두 전달
		            return SessionAssignmentConverter.toSessionAssignmentResDto(assignment, isSubmitted);
		        })
		        .toList();
		
		// sessionId를 통해서 특정 회차의 출석 목록 조회 
	    List<SessionAttendanceResDto> attendances = attendanceRepository.findAllBySessionId(sessionId)
	            .stream()
	            .map(SessionAttendanceConverter::toSessionAttendanceResDto) 
	            .toList();
		
	    
	    // 평균 출석율 구하기 
	    int attendanceRate = 0;
	    if(!attendances.isEmpty()) {
	    	long presentCount = attendances.stream()
	    			.filter(a -> a.status() == AttendanceStatus.PRESENT)
	    			.count();
	    	
	    	attendanceRate = (int) Math.round((double)presentCount / attendances.size() * 100);
	    }
	    
	    // 과제 목록 및 제출률 계산  
	    int assignmentRate = 0;
	    if(!assignmentResDtos.isEmpty()) {
	    	long submittedCount = assignmentResDtos.stream()
	    			.filter(SessionAssignmentResDto::isSubmitted)
	    			.count();
	    	
	    	assignmentRate = (int) Math.round((double) submittedCount / assignments.size() * 100);
	    }
	    
		return SessionConverter.toSessionInfoResDto(session, assignmentResDtos, attendances, attendanceRate, assignmentRate);
	}


	// 가까운 다음 회차 & 전체 회차 목록 한번에 조회
	@Override
	public SessionDashboardDataDto findSessionDashboardData(long studyId, LocalDateTime now, Pageable pageable) {
		SessionSummaryResDto nextSession = sessionRepository.findNextSessionByStudyId(studyId, now)
				.map(SessionConverter::toSessionSummaryResDto)
				.orElse(null);
		
		// 요청 Pageable 의 정렬조건과 관계없이, 무조건 최근 회차 순으로 조회하도록 구현 (가장 가까운 다음 회차기 때문에)
		Pageable pageable2 = PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				Sort.by(Sort.Direction.DESC, "startsAt"));
		
		Page<SessionSummaryResDto> sessionPage = sessionRepository.findPageByStudyId(studyId, pageable2)
				.map(SessionConverter::toSessionSummaryResDto);
		
		return new SessionDashboardDataDto(nextSession, sessionPage);
	}
	
}
