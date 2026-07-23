package com.example.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.converter.AuthConverter;
import com.example.auth.dto.request.SignUpReqDto;
import com.example.auth.dto.response.AuthResDto;
import com.example.auth.exception.code.AuthErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResDto signUp(SignUpReqDto request) {
        //중복 확인
        if (memberRepository.existsByEmail(request.email())) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        //해시값 전달
        Member member = Member.create(request.email(), passwordEncoder.encode(request.password()), request.name(), request.phone());

        return AuthConverter.toAuthResDto(memberRepository.save(member));
    }
}
