package com.example.global.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private final Long memberId;
	private final String username; // email 로 사용
	private final String password;
	private final String name;
	private final MemberStatus status;
	private final Collection<? extends GrantedAuthority> authorities;
    
    public static CustomUserDetails from(Member member) {
        return CustomUserDetails.builder()
        		.memberId(member.getId())
                .username(member.getEmail())
                .password(member.getPassword())
                .name(member.getName())
                .status(member.getStatus())
                .authorities(List.of(
                		new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
                ))
                .build();
    }
}
