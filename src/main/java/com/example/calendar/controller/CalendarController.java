package com.example.calendar.controller;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.calendar.dto.response.CalendarMonthResDto;
import com.example.calendar.service.CalendarService;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/me/calendar")
@RequiredArgsConstructor
public class CalendarController {
	
	private final CalendarService calendarService;
	
	// 특정 달 기준 회차 및 과제 조회 
	@GetMapping("")
	public CustomResponse<CalendarMonthResDto> getMonthSchedules(
			@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
	        @CurrentUser MemberPrincipal principal
	) {
		CalendarMonthResDto resDto = calendarService.getMonthSchedules(yearMonth, principal.memberId());  
		return CustomResponse.onSuccess(resDto);
	}
}
