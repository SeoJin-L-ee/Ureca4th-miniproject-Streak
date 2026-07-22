package com.example.attendance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.attendance.converter.AttendanceConverter;
import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.Participant;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
	
	private final AttendanceRepository attendanceRepository;
	private final ParticipantRepository participantRepository;
	private final SessionRepository sessionRepository;

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
							
							int attendedCount = attendanceRepository.countBySession_Study_IdAndMember_IdAndStatus(studyId, mId, AttendanceStatus.PRESENT);
							int absentCount = attendanceRepository.countBySession_Study_IdAndMember_IdAndStatus(studyId, mId, AttendanceStatus.ABSENT);
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
}
