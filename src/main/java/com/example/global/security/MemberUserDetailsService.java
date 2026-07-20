package com.example.global.security;

import com.example.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        //AuthenticationManager가 로그인 시 호출
        //DB의 암호화된 비밀번호와 요청 비밀번호를 BCrypt로 비교
        return memberRepository.findByEmail(email).map(MemberPrincipal::from).orElseThrow(() -> new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));
    }
}