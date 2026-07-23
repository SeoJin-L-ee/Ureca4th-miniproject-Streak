package com.example.member.converter;

import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.UpdateMemberResDto;
import com.example.member.entity.Member;

public class MemberConverter {

	// Member -> MemberResponse
	public static MemberResDto toMemberResponse(Member member) {
		return new MemberResDto(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone()
		);
	}

	// MemberResponse -> UpdateMemberResDto
	public static UpdateMemberResDto toMemberUpdateResDto(MemberResDto member, boolean reLoginRequired) {
		return new UpdateMemberResDto(member, reLoginRequired);
	}

}
