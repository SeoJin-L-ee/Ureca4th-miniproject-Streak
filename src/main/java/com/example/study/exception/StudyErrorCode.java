package com.example.study.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudyErrorCode implements BaseErrorCode {
	
	STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY404-0", "아이디에 해당하는 스터디가 존재하지 않습니다."),
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
