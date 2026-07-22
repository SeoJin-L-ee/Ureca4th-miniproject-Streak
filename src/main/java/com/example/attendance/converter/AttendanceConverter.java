package com.example.attendance.converter;

import java.util.List;

import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
import com.example.participant.entity.Participant;

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
	
}
