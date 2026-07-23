package com.example.submission.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubmissionErrorCode implements BaseErrorCode {
	
	DUPLICATE_SUBMISSION(HttpStatus.BAD_REQUEST, "STUDY400-0", "이미 제출한 과제입니다."),
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
