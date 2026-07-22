package com.example.member.service;

import com.example.auth.dto.response.MemberResponse;
import com.example.member.dto.request.MemberUpdateRequest;

public interface MemberService {
	
    //내 정보 조회
    MemberResponse getMyInfo(Long memberId);
    
    //내 정보 수정(이름, 전화번호, 비밀번호)
    MemberResponse updateMyInfo(Long memberId, MemberUpdateRequest request);
}