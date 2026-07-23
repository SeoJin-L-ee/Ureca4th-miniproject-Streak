package com.example.submission.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubmissionErrorCode implements BaseErrorCode {
	
	DUPLICATE_SUBMISSION(HttpStatus.BAD_REQUEST, "STUDY400-0", "이미 제출한 과제입니다."),
	SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION404-0", "아이디에 해당하는 과제 제출물이 존재하지 않습니다."),
    NOT_SUBMISSION_OWNER(HttpStatus.FORBIDDEN, "SUBMISSION403-0", "본인이 제출한 과제만 수정할 수 있습니다.")
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;
}
