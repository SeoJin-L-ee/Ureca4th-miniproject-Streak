package com.example.mypage.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.entity.enums.ApplicationStatus;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.mypage.dto.response.MyApplicationResDto;
import com.example.mypage.dto.response.MyAssignmentResDto;
import com.example.mypage.dto.response.MyAttendanceRateResDto;
import com.example.mypage.dto.response.MyPageDashboardResDto;
import com.example.mypage.dto.response.MyStreakResDto;
import com.example.mypage.dto.response.MyStudyResDto;
import com.example.mypage.dto.response.MyTodaySessionResDto;
import com.example.mypage.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    //참여 중인 스터디 목록
    @GetMapping("/studies")
    public CustomResponse<List<MyStudyResDto>> myStudies(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyStudies(principal.memberId()));
    }

    //평균 출석률
    @GetMapping("/attendance")
    public CustomResponse<MyAttendanceRateResDto> myAttendanceRate(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyAttendanceRate(principal.memberId()));
    }

    //최장 Streak
    @GetMapping("/attendance/streak")
    public CustomResponse<MyStreakResDto> myLongestStreak(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyLongestStreak(principal.memberId()));
    }

    //마감 기한 과제
    @GetMapping("/assignments")
    public CustomResponse<List<MyAssignmentResDto>> myDeadlineAssignments(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyDeadlineAssignments(principal.memberId()));
    }

    //스터디 지원 현황 조회 (?status= 로 필터링, 생략하면 전체)
    @GetMapping("/applications")
    public CustomResponse<List<MyApplicationResDto>> myApplications(@CurrentUser MemberPrincipal principal, @RequestParam(required = false) ApplicationStatus status) {
        return CustomResponse.onSuccess(myPageService.getMyApplications(principal.memberId(), status));
    }

    //오늘 회차 조회
    @GetMapping("/schedule/today")
    public CustomResponse<List<MyTodaySessionResDto>> myTodaySessions(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyTodaySessions(principal.memberId()));
    }

    //마이페이지 대시보드 - 6개 데이터를 한 번에 조회
    @GetMapping("/dashboard")
    public CustomResponse<MyPageDashboardResDto> myPageDashboard(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(myPageService.getMyPageDashboard(principal.memberId()));
    }
}
