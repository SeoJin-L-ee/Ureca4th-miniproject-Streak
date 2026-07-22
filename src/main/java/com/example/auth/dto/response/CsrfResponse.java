package com.example.auth.dto.response;

public record CsrfResponse(String token, String headerName) {
}