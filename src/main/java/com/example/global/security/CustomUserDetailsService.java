package com.example.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        String lowerEmail = email.trim().toLowerCase();

        Member member = memberRepository.findByEmail(lowerEmail)
            .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        return CustomUserDetails.from(member);
    }
}
