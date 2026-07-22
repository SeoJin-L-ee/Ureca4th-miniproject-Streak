package com.example.attendance.dto.request;

import java.util.List;

// 회차 전체 출석 저장 요청 DTO  
public record BatchSaveAttendanceReqDto(
	List<UpdateAttendanceReqDto> attendances
) {}