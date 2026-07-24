package com.example.participant.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParticipantErrorCode implements BaseErrorCode {
	
	NOT_STUDY_LEADER(HttpStatus.FORBIDDEN, "PARTICIPANT403-0", "해당 스터디의 스터디장만 지원을 처리할 수 있습니다."),
	
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT404-0", "해당하는 참여자가 존재하지 않습니다."),
	
	ALREADY_STUDY_MEMBER(HttpStatus.CONFLICT, "PARTICIPANT409-0", "이미 해당 스터디에 참여하고 있는 회원입니다."),
	
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
