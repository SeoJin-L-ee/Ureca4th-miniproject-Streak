package com.example.member.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
	
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404-0", "아이디에 해당하는 유저가 존재하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER400-0", "현재 비밀번호가 일치하지 않습니다."),
    
    ;
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
