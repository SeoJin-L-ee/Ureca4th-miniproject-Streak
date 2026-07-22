package com.example.member.dto.response;

//회원정보 수정 API 응답
//재로그인 필요 여부 확인
public record UpdateMemberResDto(MemberResDto member, boolean reLoginRequired) {
	
}