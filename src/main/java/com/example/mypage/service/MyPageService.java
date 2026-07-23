package com.example.mypage.service;

import java.util.List;

import com.example.application.entity.enums.ApplicationStatus;
import com.example.mypage.dto.response.MyApplicationResDto;
import com.example.mypage.dto.response.MyAssignmentResDto;
import com.example.mypage.dto.response.MyAttendanceRateResDto;
import com.example.mypage.dto.response.MyPageDashboardResDto;
import com.example.mypage.dto.response.MyStreakResDto;
import com.example.mypage.dto.response.MyStudyResDto;
import com.example.mypage.dto.response.MyTodaySessionResDto;

public interface MyPageService {

    //참여 중인 스터디 목록
    List<MyStudyResDto> getMyStudies(Long memberId);

    //평균 출석률
    MyAttendanceRateResDto getMyAttendanceRate(Long memberId);

    //최장 Streak
    MyStreakResDto getMyLongestStreak(Long memberId);

    //마감 기한 과제 (아직 제출 안 한 것만)
    List<MyAssignmentResDto> getMyDeadlineAssignments(Long memberId);

    //스터디 지원 현황 조회 (status가 null이면 전체)
    List<MyApplicationResDto> getMyApplications(Long memberId, ApplicationStatus status);

    //오늘 회차 조회
    List<MyTodaySessionResDto> getMyTodaySessions(Long memberId);

    //마이페이지 진입 시 필요한 모든 데이터를 한 번에 조회
    MyPageDashboardResDto getMyPageDashboard(Long memberId);
}
