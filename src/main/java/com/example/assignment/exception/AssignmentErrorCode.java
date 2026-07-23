package com.example.assignment.exception;

import org.springframework.http.HttpStatus;

import com.example.global.common.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssignmentErrorCode implements BaseErrorCode {
	
	ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSIGNMENT404-0", "아이디에 해당하는 과제가 존재하지 않습니다."),
	NOT_STUDY_ASSIGNMENT(HttpStatus.BAD_REQUEST, "ASSIGNMENT400-0", "해당 스터디에 속한 과제가 아닙니다.")
    
    ;
	
    private final HttpStatus status;
    private final String code;
    private final String message;	
}
