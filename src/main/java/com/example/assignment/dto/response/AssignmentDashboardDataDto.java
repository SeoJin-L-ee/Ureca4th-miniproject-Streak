package com.example.assignment.dto.response;

import java.util.List;
import java.util.Map;

// 대시보드 화면에 사용되는 과제 데이터 (비교 그래프 데이터, 다음 회차의 과제들, 회차별 과제 제출률) 를 묶은 DTO입니다.
public record AssignmentDashboardDataDto(
		AssignmentRateComparisonDto comparison,
		List<NextSessionAssignmentDto> nextSessionAssignments,
		Map<Long, AssignmentSubmissionRateDto> teamSubmissionRateBySessionId
) {}
