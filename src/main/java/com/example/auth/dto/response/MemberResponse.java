package com.example.auth.dto.response;

import com.example.member.entity.Member;

public record MemberResponse(Long memberId, String email, String name, String phone) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone()
        );
    }
}