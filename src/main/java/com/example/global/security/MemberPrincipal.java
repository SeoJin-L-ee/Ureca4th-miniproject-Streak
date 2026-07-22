package com.example.global.security;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

//세션에 저장될 로그인 사용자 정보
public record MemberPrincipal(
        Long memberId,
        String email,
        String password,
        String name,
        String phone,
        MemberStatus status
) implements UserDetails {

    public static MemberPrincipal from(Member member) {
        return new MemberPrincipal(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getName(),
                member.getPhone(),
                member.getStatus()
        );
    }

    //현재는 모든 ACTIVE 회원을 일반 사용자 처리
    //관리자 권한이 필요해지면 MemberRole 컬럼과 ROLE_ADMIN을 추가
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    //DISABLED 회원은 로그인 x
    @Override
    public boolean isEnabled() {
        return status == MemberStatus.ACTIVE;
    }

    //다중 세션 관리는 회원 ID 기준으로
    @Override
    public boolean equals(Object other) {
        return other instanceof MemberPrincipal principal
                && memberId.equals(principal.memberId);
    }

    @Override
    public int hashCode() {
        return memberId.hashCode();
    }
}