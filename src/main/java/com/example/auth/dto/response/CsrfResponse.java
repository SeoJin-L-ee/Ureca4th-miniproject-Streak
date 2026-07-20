package com.example.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CsrfResponse {

    private String token;
    private String headerName;
}