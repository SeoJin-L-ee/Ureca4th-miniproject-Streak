package com.example.study.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.example.assignment.dto.response.AssignmentDashboardDataDto;
import com.example.assignment.dto.response.AssignmentSubmissionRateDto;
import com.example.attendance.dto.response.AttendanceDashboardDataDto;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.session.dto.response.MergedSessionDashboardDataDto;
import com.example.session.dto.response.SessionDashboardDataDto;
import com.example.session.dto.response.SessionDashboardDataListResDto;
import com.example.session.dto.response.SessionSummaryResDto;
import com.example.study.dto.response.StudyDashboardResDto;
import com.example.study.entity.Study;

public class StudyDashboardConverter {
	
	public static StudyDashboardResDto toStudyDashboardResDto(
			Study study,
			boolean isLeader,
			long currentApplicationCnt,
			long currentParticipantCnt,
			SessionDashboardDataDto sessionData,
			AttendanceDashboardDataDto attendanceData,
			AssignmentDashboardDataDto assignmentData,
			LocalDateTime now
	) {
		Page<SessionSummaryResDto> sessionPage = sessionData.sessions();
		List<MergedSessionDashboardDataDto> sessionItems = sessionPage.getContent().stream()
				.map(session -> {
					long sessionId = session.sessionId();
					// 출석 데이터 추출
					AttendanceStatus myAttendanceStatus = attendanceData.myStatusBySessionId().get(sessionId);
					Double teamAttendanceRate = attendanceData.teamAttendanceRateBySessionId().get(sessionId);
					// 과제 데이터 추출
					AssignmentSubmissionRateDto submissionRate =
							assignmentData.teamSubmissionRateBySessionId().get(sessionId);
					boolean hasAssignments = submissionRate != null && submissionRate.hasAssignments();
					// submissionRate 가 null 일 경우를 대비해서 삼항연산자로 체크 
					Double teamAssignmentSubmissionRate = hasAssignments ? submissionRate.teamSubmissionRate() : null;
					
					// 다 합쳐서 MergedSessionDashboardDataDto 반환
					return new MergedSessionDashboardDataDto(
							sessionId,
							session.sessionNumber(),
							session.title(),
							session.startsAt(),
							myAttendanceStatus,
							teamAttendanceRate,
							teamAssignmentSubmissionRate,
							hasAssignments);
				}).toList();
		
		SessionDashboardDataListResDto sessionDashboardDataList = new SessionDashboardDataListResDto(
				sessionPage.getNumber(),
				sessionPage.getSize(),
				sessionPage.getTotalPages(),
				sessionPage.getTotalElements(),
				sessionPage.hasNext(),
				sessionItems);
		
		return new StudyDashboardResDto(
				study.getId(),
				study.getTitle(),
				study.getDescription(),
				study.getCategory(),
				study.getStatus(),
				isLeader,
				currentApplicationCnt,
				currentParticipantCnt,
				study.getCapacity(),
				
				sessionData.nextSession(),
				assignmentData.nextSessionAssignments(),
				
				attendanceData.comparison(),
				assignmentData.comparison(),
				
				sessionDashboardDataList
		);
	}
}
