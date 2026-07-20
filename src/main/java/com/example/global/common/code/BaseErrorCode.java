package com.example.global.common.code;

import org.springframework.http.HttpStatus;

import com.example.global.common.CustomResponse;

public interface BaseErrorCode {

    HttpStatus getStatus();
    String getCode();
    String getMessage();

    default CustomResponse<Void> getErrorResponse() {
        return CustomResponse.onFailure(getCode(), getMessage());
    }
}
