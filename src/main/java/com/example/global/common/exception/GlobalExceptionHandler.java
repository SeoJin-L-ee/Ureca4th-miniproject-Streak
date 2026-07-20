package com.example.global.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.global.common.CustomResponse;
import com.example.global.common.code.BaseErrorCode;
import com.example.global.common.code.CommonErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 에러 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<CustomResponse<Void>> handleCustomException(GeneralException ex) {

        log.warn("[ CustomException ]: {}", ex.getCode().getMessage());
        return ResponseEntity
                .status(ex.getCode().getStatus())
                .body(ex.getCode().getErrorResponse());
    }

    // @Valid 및 Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        BaseErrorCode validationErrorCode = CommonErrorCode.NOT_VALID_ERROR;
        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                validationErrorCode.getCode(),
                validationErrorCode.getMessage(),
                errors
        );
        return ResponseEntity
                .status(validationErrorCode.getStatus())
                .body(errorResponse);
    }

    // 이외의 모든 예외 처리 (최상위 예외)
    @ExceptionHandler({Exception.class})
    public ResponseEntity<CustomResponse<String>> handleAllException(Exception ex) {

        log.error("[ WARNING ] Unhandled Exception : {} ", ex.getMessage());
        BaseErrorCode baseErrorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        CustomResponse<String> errorResponse = CustomResponse.onFailure(
                baseErrorCode.getCode(),
                baseErrorCode.getMessage(),
                null
        );
        return ResponseEntity
                .status(baseErrorCode.getStatus())
                .body(errorResponse);
    }
}
