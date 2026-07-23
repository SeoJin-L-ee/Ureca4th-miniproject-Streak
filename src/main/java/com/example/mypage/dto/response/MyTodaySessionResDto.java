package com.example.mypage.dto.response;

import java.time.LocalDateTime;

//마이페이지 - 오늘 회차 조회
public record MyTodaySessionResDto(Long sessionId, Long studyId, String studyTitle, String title, LocalDateTime startsAt) {

}
