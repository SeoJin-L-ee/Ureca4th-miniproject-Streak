package com.example.attendance.converter;

import java.util.List;

import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
import com.example.attendance.dto.response.AttendanceParticipantResDto;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.session.entity.Session;

public class AttendanceConverter {
	
	// AttendanceMemberResDto 생성 
	public static AttendanceMemberResDto toAttendanceMemberResDto(Participant participant, int attendedCount, int absentCount, double rate) {
		
		return new AttendanceMemberResDto(
				participant.getMember().getId(),
				participant.getMember().getName(),
				attendedCount,
				absentCount,
				rate
		);
	}
	
	// AttendanceMemberResDto -> AttendanceListResDto
	public static AttendanceListResDto toAttendanceListResDto(
			long studyId, 
			int totalSessionCount,
			double totalAvgRate, 
			List<AttendanceMemberResDto> memberResDtos
	) {
		return new AttendanceListResDto(
				studyId,
				totalSessionCount,
				totalAvgRate,
				memberResDtos
		);
	}
	
	// AttendanceParticipantResDto 생성 
	public static AttendanceParticipantResDto toAttendanceParticipantResDto(
			Member member, 
			AttendanceStatus status
	) {
		return new AttendanceParticipantResDto(
                member.getId(),
                member.getName(),
                status
        );
	}
	
	
	// Session, Member, Status를 기반으로 Attendance 엔티티 생성 
	public static Attendance toEntity(Session session, Member member, AttendanceStatus status) {
	    return Attendance.builder()
	            .session(session)
	            .member(member)
	            .status(status)
	            .build();
	}
}
