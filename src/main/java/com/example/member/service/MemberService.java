package com.example.member.service;

import java.util.List;

import com.example.application.entity.enums.ApplicationStatus;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.MyApplicationResDto;
import com.example.member.dto.response.MyAssignmentResDto;
import com.example.member.dto.response.MyAttendanceRateResDto;
import com.example.member.dto.response.MyStreakResDto;
import com.example.member.dto.response.MyStudyResDto;
import com.example.member.dto.response.MyTodaySessionResDto;

public interface MemberService {
	
    //내 정보 조회
    MemberResDto getMyInfo(Long memberId);

    //내 정보 수정(이름, 전화번호, 비밀번호)
    MemberResDto updateMyInfo(Long memberId, UpdateMemberReqDto request);

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
}