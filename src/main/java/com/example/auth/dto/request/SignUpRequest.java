package com.example.auth.dto.request;

public record SignUpRequest(
	// validation 도 추가할 것
    String email,
    String password,
    String name,
    String phone
) {
}
