package com.example.calendar.converter;

import com.example.assignment.entity.Assignment;
import com.example.calendar.dto.response.CalendarItemResDto;
import com.example.session.entity.Session;

public class CalendarConverter {
	
	// Session 엔티티를 CalendarItemResDto로 변환 
	public static CalendarItemResDto fromSession(Session session) {
        return new CalendarItemResDto(
            "SESSION",
            session.getId(),
            session.getStudy().getId(),
            session.getStudy().getTitle(),
            session.getTitle(),
            session.getStartsAt()
        );
    }

    // Assignment 엔티티를 CalendarItemResDto로 변환
    public static CalendarItemResDto fromAssignment(Assignment assignment) {
        return new CalendarItemResDto(
            "ASSIGNMENT",
            assignment.getId(),
            assignment.getSession().getStudy().getId(),
            assignment.getSession().getStudy().getTitle(),
            assignment.getTitle(),
            assignment.getDueAt()
        );
    }
}
