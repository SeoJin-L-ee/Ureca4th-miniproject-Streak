package com.example.study.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudyErrorCode implements BaseErrorCode {
	
	STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY404-0", "해당 스터디를 찾을 수 없습니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
