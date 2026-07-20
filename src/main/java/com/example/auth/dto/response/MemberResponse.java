package com.example.auth.dto.response;

import com.example.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {

    private Long memberId;
    private String email;
    private String name;
    private String phone;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone()
        );
    }
}