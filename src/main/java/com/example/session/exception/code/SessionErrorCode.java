package com.example.session.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionErrorCode implements BaseErrorCode {
	
	SESSION_CREATE_FAILED(HttpStatus.BAD_REQUEST, "SESSION400-1", "회차를 생성할 수 없습니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
