package com.example.auth.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH400-0", "이미 가입된 이메일입니다."),

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
