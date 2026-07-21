package com.example.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.repository.MemberRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class MemberUserDetailsServiceTest {
	
	@Mock
	MemberRepository memberRepository;
	
	@InjectMocks
	MemberUserDetailsService memberUserDetailsService;
	
	@Test
	@DisplayName("존재하는 이메일이면 MemberPrincipal 반환")
	void goodEmailReturnMemberPrincipal() {
		Member member = Member.builder().id(1L).email("test@test.com").password("sadfgr").name("길동이").status(MemberStatus.ACTIVE).build();
		
		given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
		
		UserDetails userDetails = memberUserDetailsService.loadUserByUsername("test@test.com");
		
		log.info("[검증] loadUserByUsername(\"test@test.com\") -> class={}, username={}", userDetails.getClass().getSimpleName(), userDetails.getUsername());
		
		assertThat(userDetails).isInstanceOf(MemberPrincipal.class);
		assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
	}
	
	@Test
	@DisplayName("존재하지 않는 이메일이면 예외")
	void notFountEmailException() {
		given(memberRepository.findByEmail("asdf@test.com")).willReturn(Optional.empty());
		
		log.info("[검증] 존재하지 않는 이메일로 조회 시 UsernameNotFoundException 발생 여부 확인");
		
		assertThatThrownBy(() -> memberUserDetailsService.loadUserByUsername("asdf@test.com")).isInstanceOf(UsernameNotFoundException.class);
	}
}
