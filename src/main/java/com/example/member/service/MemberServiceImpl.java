package com.example.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global.common.exception.GeneralException;
import com.example.member.converter.MemberConverter;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.entity.Member;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //@CurrentUser로 받은 memberId는 이미 인증된 세션의 값이라 이론상 항상 존재하지만, 방어적으로 예외 처리
    @Override
    public MemberResDto getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberConverter.toMemberResponse(member);
    }

    @Override
    @Transactional
    public MemberResDto updateMyInfo(Long memberId, UpdateMemberReqDto request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        //이름/전화번호는 항상 수정 대상 (필수 입력)
        member.updateProfile(request.name(), request.phone());

        //newPassword가 있을 때만 비밀번호 변경
        if (request.newPassword() != null) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
                throw new GeneralException(MemberErrorCode.PASSWORD_MISMATCH);
            }
            member.changePassword(passwordEncoder.encode(request.newPassword()));
        }

        return MemberConverter.toMemberResponse(member);
    }
}
