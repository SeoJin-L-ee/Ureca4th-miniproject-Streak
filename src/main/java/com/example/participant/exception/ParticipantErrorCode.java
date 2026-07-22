package com.example.participant.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParticipantErrorCode implements BaseErrorCode {
	
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT404-0", "해당하는 참여자가 존재하지 않습니다."),
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
