package com.example.session.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionErrorCode implements BaseErrorCode {
	
	SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION404-0", "존재하지 않는 회차입니다."),
	NOT_STUDY_SESSION(HttpStatus.BAD_REQUEST, "SESSION400-0", "해당 스터디에 속한 회차가 아닙니다."),
	DUPLICATE_SESSION_NUMBER(HttpStatus.CONFLICT, "SESSION409-0", "이미 존재하는 회차 번호입니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
