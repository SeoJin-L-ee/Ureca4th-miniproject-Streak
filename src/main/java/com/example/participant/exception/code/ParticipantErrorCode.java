package com.example.participant.exception.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParticipantErrorCode implements BaseErrorCode {
	
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT404-0", "해당 참여자를 찾을 수 없습니다."),
	PARTICIPANT_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "PARTICIPANT401-0", "권한이 없습니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
