package com.example.calendar.service;

import java.time.YearMonth;

import com.example.calendar.dto.response.CalendarMonthResDto;

public interface CalendarService {
	
	// 월 기준 사용자의 회차 및 과제 조회 
	CalendarMonthResDto getMonthSchedules(YearMonth yearMonth, long memberId);
}
