package com.example.global.security;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class MemberPrincipal implements UserDetails {

    //세션에 저장될 로그인 사용자 정보
    private final Long memberId;
    private final String email;
    private final String password;
    private final String name;
    private final String phone;
    private final MemberStatus status;

    private MemberPrincipal(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.status = member.getStatus();
    }

    public static MemberPrincipal from(Member member) {
        return new MemberPrincipal(member);
    }

    //현재는 모든 ACTIVE 회원을 일반 사용자 처리
    //관리자 권한이 필요해지면 MemberRole 컬럼과 ROLE_ADMIN을 추가
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
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