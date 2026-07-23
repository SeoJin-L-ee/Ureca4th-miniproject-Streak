package com.example.mypage.converter;

import com.example.application.entity.Application;
import com.example.assignment.entity.Assignment;
import com.example.mypage.dto.response.MyApplicationResDto;
import com.example.mypage.dto.response.MyAssignmentResDto;
import com.example.mypage.dto.response.MyStudyResDto;
import com.example.mypage.dto.response.MyTodaySessionResDto;
import com.example.participant.entity.Participant;
import com.example.session.entity.Session;

public class MyPageConverter {

	// Participant -> MyStudyResDto (참여 중인 스터디 목록)
	public static MyStudyResDto toMyStudyResDto(Participant participant) {
		return new MyStudyResDto(participant.getStudy().getId(), participant.getStudy().getTitle(),	participant.getStudy().getCategory(),
									participant.getStudy().getStatus(), participant.getRole());
	}

	// Assignment -> MyAssignmentResDto (마감 기한 과제 목록)
	public static MyAssignmentResDto toMyAssignmentResDto(Assignment assignment) {
		return new MyAssignmentResDto(assignment.getId(), assignment.getSession().getStudy().getId(), assignment.getSession().getStudy().getTitle(),
										assignment.getTitle(), assignment.getDueAt());
	}

	// Application -> MyApplicationResDto (스터디 지원 현황 조회)
	public static MyApplicationResDto toMyApplicationResDto(Application application) {
		return new MyApplicationResDto(application.getId(), application.getStudy().getId(), application.getStudy().getTitle(), application.getStatus(), application.getCreatedAt());
	}

	// Session -> MyTodaySessionResDto (오늘 회차 조회)
	public static MyTodaySessionResDto toMyTodaySessionResDto(Session session) {
		return new MyTodaySessionResDto(session.getId(), session.getStudy().getId(), session.getStudy().getTitle(), session.getTitle(), session.getStartsAt());
	}
}
