package com.example.application.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationErrorCode implements BaseErrorCode {

	INVALID_UPDATE_STATUS(HttpStatus.BAD_REQUEST, "APPLICATION400-0", "지원 상태는 승인 또는 거절 상태로만 변경할 수 있습니다."),
	
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION404-0", "아이디에 해당하는 스터디 지원 내역이 존재하지 않습니다."),
	
	APPLICATION_ALREADY_PENDING(HttpStatus.CONFLICT, "APPLICATION409-0", "이미 해당 스터디에 처리 대기 중인 지원 내역이 존재합니다."),
	APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "APPLICATION409-1", "이미 해당 스터디에 지원한 내역이 존재합니다."),
	APPLICATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "APPLICATION409-2", "이미 승인 또는 거절 처리된 지원입니다."),
	
	
	;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
