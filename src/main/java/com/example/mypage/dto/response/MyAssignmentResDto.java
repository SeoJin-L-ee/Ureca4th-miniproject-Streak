package com.example.mypage.dto.response;

import java.time.LocalDateTime;

//마이페이지 - 마감 기한 과제
public record MyAssignmentResDto(Long assignmentId, Long studyId, String studyTitle, String title, LocalDateTime dueAt) {

}
