package com.example.mypage.dto.response;

import java.util.List;

//마이페이지 진입 시 필요한 데이터를 한 번에 담는 DTO
public record MyPageDashboardResDto(
									List<MyStudyResDto> studies,
									MyAttendanceRateResDto attendanceRate,
									MyStreakResDto streak,
									List<MyAssignmentResDto> deadlineAssignments,
									List<MyTodaySessionResDto> todaySessions,
									List<MyApplicationResDto> applications
									) {

}
