package com.example.member.service;

import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;

public interface MemberService {

    //내 정보 조회
    MemberResDto getMyInfo(Long memberId);

    //내 정보 수정(이름, 전화번호, 비밀번호)
    MemberResDto updateMyInfo(Long memberId, UpdateMemberReqDto request);
}
