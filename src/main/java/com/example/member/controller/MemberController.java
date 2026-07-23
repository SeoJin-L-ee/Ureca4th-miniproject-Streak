package com.example.member.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.entity.enums.ApplicationStatus;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.member.converter.MemberConverter;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.MyApplicationResDto;
import com.example.member.dto.response.MyAssignmentResDto;
import com.example.member.dto.response.MyAttendanceRateResDto;
import com.example.member.dto.response.MyStreakResDto;
import com.example.member.dto.response.MyStudyResDto;
import com.example.member.dto.response.MyTodaySessionResDto;
import com.example.member.dto.response.UpdateMemberResDto;
import com.example.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public CustomResponse<MemberResDto> me(@CurrentUser MemberPrincipal principal) {
    	
    	//인증되지 않은 사용자는 401 처리
        return CustomResponse.onSuccess(memberService.getMyInfo(principal.memberId()));
    }

    @PatchMapping("/me")
    public CustomResponse<UpdateMemberResDto> updateMe(@CurrentUser MemberPrincipal principal,
												            @Valid @RequestBody UpdateMemberReqDto request,
												            HttpServletRequest servletRequest,
												            HttpServletResponse servletResponse,
												            Authentication authentication
												            )
    {
        MemberResDto response = memberService.updateMyInfo(principal.memberId(), request);

        //비밀번호 변경 시 재로그인
        boolean reLoginRequired = request.newPassword() != null;
        
        //세션 무효화 + SecurityContext 초기화 = 비밀번호 변경 시.
        if (reLoginRequired) {
            new SecurityContextLogoutHandler().logout(servletRequest, servletResponse, authentication);
        }

        return CustomResponse.onSuccess(MemberConverter.toMemberUpdateResDto(response, reLoginRequired));
    }

    //참여 중인 스터디 목록
    @GetMapping("/me/studies")
    public CustomResponse<List<MyStudyResDto>> myStudies(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(memberService.getMyStudies(principal.memberId()));
    }

    //평균 출석률
    @GetMapping("/me/attendance")
    public CustomResponse<MyAttendanceRateResDto> myAttendanceRate(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(memberService.getMyAttendanceRate(principal.memberId()));
    }

    //최장 Streak
    @GetMapping("/me/attendance/streak")
    public CustomResponse<MyStreakResDto> myLongestStreak(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(memberService.getMyLongestStreak(principal.memberId()));
    }

    //마감 기한 과제
    @GetMapping("/me/assignments")
    public CustomResponse<List<MyAssignmentResDto>> myDeadlineAssignments(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(memberService.getMyDeadlineAssignments(principal.memberId()));
    }

    //스터디 지원 현황 조회 (?status= 로 필터링, 생략하면 전체)
    @GetMapping("/me/applications")
    public CustomResponse<List<MyApplicationResDto>> myApplications(@CurrentUser MemberPrincipal principal, @RequestParam(required = false) ApplicationStatus status) {
        return CustomResponse.onSuccess(memberService.getMyApplications(principal.memberId(), status));
    }

    //오늘 회차 조회
    @GetMapping("/me/schedule/today")
    public CustomResponse<List<MyTodaySessionResDto>> myTodaySessions(@CurrentUser MemberPrincipal principal) {
        return CustomResponse.onSuccess(memberService.getMyTodaySessions(principal.memberId()));
    }
}