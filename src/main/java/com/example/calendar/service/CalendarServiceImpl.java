package com.example.calendar.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.calendar.converter.CalendarConverter;
import com.example.calendar.dto.response.CalendarItemResDto;
import com.example.calendar.dto.response.CalendarMonthResDto;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {
	
	private final SessionRepository sessionRepository;
	private final AssignmentRepository assignmentRepository;
	
	// 월 기준으로 사용자의 회차와 과제를 조회 
	@Override
	public CalendarMonthResDto getMonthSchedules(YearMonth yearMonth, long memberId) {
		
		// 월 시작일 / 종료일 (이전 주, 다음 주 여유분도 포함) 
		LocalDateTime start = yearMonth.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay();
		LocalDateTime end = yearMonth.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atTime(23, 59, 59);
		
		// 해당 사용자의 회차 조회 
		List<Session> sessions = sessionRepository.findByMemberIdAndDateRange(memberId, start, end);
		
		List<CalendarItemResDto> sessionDtos = sessions.stream()
					.map(CalendarConverter::fromSession)
					.toList();
		
		// 해당 사용자의 과제 조회 
		List<Assignment> assignments = assignmentRepository.findByMemberIdAndDateRange(memberId, start, end);
		
		List<CalendarItemResDto> assignmentDtos = assignments.stream()
					.map(CalendarConverter::fromAssignment)
					.toList();
		
		// session과 assignment를 합쳐서 하나의 일정 list로 만듦 
		List<CalendarItemResDto> allSchedules = Stream.concat(
						sessionDtos.stream(), 
						assignmentDtos.stream()
					)
					// date가 null 인 객체는 맨 뒤로 보냄, 나머지는 날짜 오름차순 정렬 
					.sorted(Comparator.comparing(CalendarItemResDto::date, Comparator.nullsLast(Comparator.naturalOrder())))	
		            .toList();
		
		return new CalendarMonthResDto(
	            yearMonth.getYear(),
	            yearMonth.getMonthValue(),
	            allSchedules
	    );
	}
}
