package com.example.auth.converter;

import com.example.auth.dto.response.AuthResDto;
import com.example.member.entity.Member;

public class AuthConverter {

	// Member -> AuthResDto
	public static AuthResDto toAuthResDto(Member member) {
		return new AuthResDto(
				member.getId(),
				member.getEmail(),
				member.getName(),
				member.getPhone()
		);
	}

}
