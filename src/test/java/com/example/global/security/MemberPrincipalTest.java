package com.example.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberPrincipalTest {

    private Member activeMember() {
        return Member.builder().id(1L).email("test@test.com").password("qwer1212").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("Member필드를 그대로 옮김")
    void fieldsFromMemberAll() {
        Member member = activeMember();

        MemberPrincipal principal = MemberPrincipal.from(member);
        
        log.info("[검증] email: member={} -> principal={}", member.getEmail(), principal.email());
        log.info("[검증] password: member={} -> principal={}", member.getPassword(), principal.password());

        assertThat(principal.memberId()).isEqualTo(member.getId());
        assertThat(principal.email()).isEqualTo(member.getEmail());
        assertThat(principal.password()).isEqualTo(member.getPassword());
        assertThat(principal.name()).isEqualTo(member.getName());
        assertThat(principal.phone()).isEqualTo(member.getPhone());
    }

    @Test
    @DisplayName("getUsername() - 이메일 반환")
    void returnsEmailAsUsername() {
        MemberPrincipal principal = MemberPrincipal.from(activeMember());
        
        log.info("[검증] getUsername() 결과 = {}", principal.getUsername());

        assertThat(principal.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("ACTIVE회원은 isEnabled()가 true")
    void isEnabledTrueStatusIsActive() {
    	MemberPrincipal principal = MemberPrincipal.from(activeMember());
    	log.info("[검증] status=ACTIVE, isEnabled()={}", principal.isEnabled());
    	
        assertThat(MemberPrincipal.from(activeMember()).isEnabled()).isTrue();
    }

    @Test
    @DisplayName("DISABLED회원은 isEnabled()가 false")
    void isDisabledFalseStatusIsDisabled() {
        Member disabled = Member.builder().id(2L).email("d@test.com").password("pw").name("비활성").status(MemberStatus.DISABLED).build();
        
        MemberPrincipal principal = MemberPrincipal.from(disabled);
        log.info("[검증] status=DISABLED, isEnabled()={}", principal.isEnabled());

        assertThat(MemberPrincipal.from(disabled).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("권한은 일단 ROLE_USER 하나만")
    void hasOnlyUserRole() {
        MemberPrincipal principal = MemberPrincipal.from(activeMember());

        log.info("[검증] authorities = {}", principal.getAuthorities());
        
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("equals와 hashCode는 memberId 기준으로")
    void equalsAndHashCodeBaseOnMemberId() {
        MemberPrincipal p1 = MemberPrincipal.from(activeMember());
        Member sameIdDifferentEmail = Member.builder().id(1L).email("other@test.com").password("pw").name("다른이름").status(MemberStatus.ACTIVE).build();
        MemberPrincipal p2 = MemberPrincipal.from(sameIdDifferentEmail);

        log.info("[검증] p1(email={}) vs p2(email={}) -> equals={}", p1.email(), p2.email(), p1.equals(p2));
        
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }
}