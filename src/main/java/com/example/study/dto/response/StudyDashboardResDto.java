package com.example.study.dto.response;

import java.util.List;

import com.example.assignment.dto.response.AssignmentRateComparisonDto;
import com.example.assignment.dto.response.NextSessionAssignmentDto;
import com.example.attendance.dto.response.AttendanceRateComparisonDto;
import com.example.session.dto.response.SessionDashboardDataListResDto;
import com.example.session.dto.response.SessionSummaryResDto;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

// 스터디 상세 조회 (대시보드) 화면에서 보여지는 모든 데이터를 묶은 DTO입니다.
public record StudyDashboardResDto(
		Long studyId,
		String title,
		String description,
		StudyCategory category,
		StudyStatus status,
		boolean isLeader,
		// 스터디장한테만 실제 값, 참여자한테는 그냥 0
		long currentApplicationCnt,
		long currentParticipantCount,
		int capacity,
		
		// 다음 회차 데이터 및 다음 회차의 과제 데이터
		SessionSummaryResDto nextSession,
		List<NextSessionAssignmentDto> nextSessionAssignments,
		
		// 비교 그래프 데이터
		AttendanceRateComparisonDto attendanceComparison,
		AssignmentRateComparisonDto assignmentComparison,
		
		// 전체 회차 목록 페이징 데이터
		SessionDashboardDataListResDto sessions
) {}
