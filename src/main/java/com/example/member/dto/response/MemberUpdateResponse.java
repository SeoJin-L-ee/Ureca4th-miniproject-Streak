package com.example.member.dto.response;

import com.example.auth.dto.response.MemberResponse;

//회원정보 수정 API 응답
//재로그인 필요 여부 확인
public record MemberUpdateResponse(MemberResponse member, boolean reLoginRequired) {

    public static MemberUpdateResponse of(MemberResponse member, boolean reLoginRequired) {
        return new MemberUpdateResponse(member, reLoginRequired);
    }
}