package com.example.attendance.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.attendance.converter.AttendanceConverter;
import com.example.attendance.dto.request.BatchSaveAttendanceReqDto;
import com.example.attendance.dto.request.UpdateAttendanceReqDto;
import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
import com.example.attendance.dto.response.AttendanceParticipantResDto;
import com.example.attendance.dto.response.AttendanceSessionResDto;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.exception.code.SessionErrorCode;
import com.example.session.repository.SessionRepository;
import com.example.study.exception.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
	
	private final AttendanceRepository attendanceRepository;
	private final ParticipantRepository participantRepository;
	private final SessionRepository sessionRepository;
	private final StudyRepository studyRepository;

	// 스터디 내 참여자별 출석 현황 조회 
	@Override
	public AttendanceListResDto getMemberAttendances(long studyId, long memberId) {
		
		// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 스터디 참여자 목록 조회 
		List<Participant> participants = participantRepository.findAllByStudyId(studyId);
		
		// 각 참여자별 출석 데이터 집계 및 DTO 변환 
		List<AttendanceMemberResDto> memberResDtos = participants.stream()
						.map(participant -> {
							long mId = participant.getMember().getId();
							
							int attendedCount = attendanceRepository.countBySessionStudyIdAndMemberIdAndStatus(studyId, mId, AttendanceStatus.PRESENT);
							int absentCount = attendanceRepository.countBySessionStudyIdAndMemberIdAndStatus(studyId, mId, AttendanceStatus.ABSENT);
							int total = attendedCount + absentCount;
							
							double rate = (total == 0) ? 0.0 : Math.round(((double) attendedCount / total * 100) * 10) / 10.0;
							
							return AttendanceConverter.toAttendanceMemberResDto(participant, attendedCount, absentCount, rate);
						})
						.toList();
		
		
		// 스터디 전체 평균 출석률 계산 
		double totalAvgRate = memberResDtos.stream()
						.mapToDouble(AttendanceMemberResDto::attendanceRate)
						.average()
						.orElse(0.0);
		
		double roundedAvg = Math.round(totalAvgRate * 10) / 10.0; 
		
		int totalSessionCount = sessionRepository.countByStudyId(studyId);
		
		return AttendanceConverter.toAttendanceListResDto(studyId, totalSessionCount, roundedAvg, memberResDtos);
	}

	
	// 회차별 참여자 출석 목록 조회 
	@Override
	public AttendanceSessionResDto getSessionAttendances(long studyId, long sessionId, long memberId) {
		
		if (!studyRepository.existsById(studyId)) throw new GeneralException(StudyErrorCode.STUDY_NOT_FOUND);
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
		
		// LEADER 로 등록된 Member 만 스터디 회차를 생성할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		List<Member> members = participantRepository.findMembersByStudyId(studyId);
		
		Map<Long, AttendanceStatus> attendanceMap = attendanceRepository.findAllBySessionId(sessionId)
						.stream()
						.collect(Collectors.toMap(a -> a.getMember().getId(), Attendance::getStatus));
		
		List<AttendanceParticipantResDto> participants = members.stream()
	            		.map(member -> AttendanceConverter.toAttendanceParticipantResDto(member, attendanceMap.get(member.getId())))
	            		.toList();
		
		return new AttendanceSessionResDto(sessionId, participants);
	}


	// 참여자 출석 사항 저장 - 스터디장 전용 
	@Override
	@Transactional
	public void updateSessionAttendances(
			long studyId, long sessionId, 
			long memberId, 
			BatchSaveAttendanceReqDto reqDto
	) {
		
		if (!studyRepository.existsById(studyId)) throw new GeneralException(StudyErrorCode.STUDY_NOT_FOUND);
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
		
		// LEADER 로 등록된 Member 만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		Session session = sessionRepository.findById(sessionId)
						.orElseThrow(() -> new GeneralException(SessionErrorCode.SESSION_NOT_FOUND));
		
		
		// 해당 회차에 이미 존재하던 출석 데이터 Map 변환
        Map<Long, Attendance> existingAttendanceMap = attendanceRepository.findAllBySessionId(sessionId)
                		.stream()
                		.collect(Collectors.toMap(a -> a.getMember().getId(), a -> a));

        List<Attendance> attendancesToSave = new ArrayList<>();

        for (UpdateAttendanceReqDto dto : reqDto.attendances()) {
        	
            if (existingAttendanceMap.containsKey(dto.memberId())) {
            	
                // 이미 기존 기록이 존재하면 status 업데이트
                Attendance attendance = existingAttendanceMap.get(dto.memberId());
                attendance.updateStatus(dto.status());
                
            } else {
            	
                // 기존 기록이 없으면 신규 Attendance 엔티티 생성 후 추가
                Member member = participantRepository.findMemberByStudyIdAndMemberId(studyId, dto.memberId())
                        	.orElseThrow(() -> new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND));

                Attendance newAttendance = AttendanceConverter.toEntity(session, member, dto.status());
                attendancesToSave.add(newAttendance);
            }
        }

        // 새로 추가된 출석 건 일괄 저장
        if (!attendancesToSave.isEmpty()) {
            attendanceRepository.saveAll(attendancesToSave);
        }
	}
}
