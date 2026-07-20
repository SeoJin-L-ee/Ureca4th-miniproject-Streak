package com.example.application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/applications/")
@RequiredArgsConstructor
public class TestController {

	@GetMapping("success-test")
	public CustomResponse<String> getSTest() {
		return CustomResponse.onSuccess("test Success");
	}
	
	@GetMapping("fail-test")
	public CustomResponse<String> getFTest() {
		throw new GeneralException(CommonErrorCode.INTERNAL_SERVER_ERROR);
//		return CustomResponse.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "test Fail");
	}
}
