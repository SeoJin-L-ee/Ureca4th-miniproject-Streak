package com.example.auth.service;

import com.example.auth.dto.request.SignUpRequest;
import com.example.auth.dto.response.MemberResponse;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        //중복 확인
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        //해시값 전달
        Member member = Member.create(request.email(), passwordEncoder.encode(request.password()), request.name(), request.phone());

        return MemberResponse.from(memberRepository.save(member));
    }
}