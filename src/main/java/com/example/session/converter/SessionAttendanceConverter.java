package com.example.session.converter;

import com.example.attendance.entity.Attendance;
import com.example.session.dto.response.SessionAttendanceResDto;

public class SessionAttendanceConverter {
	
	// 회차 상세 조회 시 출석 정보를 응답 DTO로 변환 
	public static SessionAttendanceResDto toSessionAttendanceResDto(Attendance attendance) {
        return new SessionAttendanceResDto(
                attendance.getId(),
                attendance.getMember().getId(), 
                attendance.getMember().getName(), 
                attendance.getStatus()
        );
    }
}
