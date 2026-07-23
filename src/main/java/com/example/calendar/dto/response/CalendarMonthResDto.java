package com.example.calendar.dto.response;

import java.util.List;

// 월 조회 Calendar 응답 DTO 
public record CalendarMonthResDto(
	int year,
	int month,
	List<CalendarItemResDto> schedules
) {}
