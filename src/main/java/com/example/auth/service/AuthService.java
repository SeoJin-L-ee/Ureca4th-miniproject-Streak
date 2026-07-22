package com.example.auth.service;

import com.example.auth.dto.request.SignUpReqDto;
import com.example.auth.dto.response.AuthResDto;

public interface AuthService {

    AuthResDto signUp(SignUpReqDto request);
}
