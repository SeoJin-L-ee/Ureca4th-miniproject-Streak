package com.example.mypage.dto.response;

import java.time.LocalDateTime;

import com.example.application.entity.enums.ApplicationStatus;

//마이페이지 - 스터디 지원 현황 조회
public record MyApplicationResDto(Long applicationId, Long studyId, String studyTitle, ApplicationStatus status, LocalDateTime appliedAt) {

}
