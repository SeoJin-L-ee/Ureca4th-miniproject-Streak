package com.example.member.converter;

import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.UpdateMemberResDto;
import com.example.member.entity.Member;

public class MemberConverter {

	// Member -> MemberResDto (내 정보 조회)
	public static MemberResDto toMemberResponse(Member member) {
		return new MemberResDto(member.getId(), member.getEmail(), member.getName(), member.getPhone());
	}

	// MemberResponse -> UpdateMemberResDto (내 정보 수정)
	public static UpdateMemberResDto toMemberUpdateResDto(MemberResDto member, boolean reLoginRequired) {
		return new UpdateMemberResDto(member, reLoginRequired);
	}
}
