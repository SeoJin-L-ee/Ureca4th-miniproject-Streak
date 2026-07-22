package com.example.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.global.common.exception.GeneralException;
import com.example.member.MemberErrorCode;
import com.example.member.dto.request.MemberUpdateRequest;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.repository.MemberRepository;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks MemberServiceImpl memberService;

    private Member member() {
        return Member.builder().id(1L).email("test@test.com").password("encodedOld").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("이름/전화번호만 수정하면 비밀번호는 그대로")
    void updateProfileOnly() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberService.updateMyInfo(1L, new MemberUpdateRequest("변경이름", "010-9999-9999", null, null));

        log.info("[검증] updateProfileOnly -> name={}, phone={}, password={}", member.getName(), member.getPhone(), member.getPassword());

        assertThat(member.getName()).isEqualTo("변경이름");
        assertThat(member.getPhone()).isEqualTo("010-9999-9999");
        assertThat(member.getPassword()).isEqualTo("encodedOld");
    }

    @Test
    @DisplayName("현재 비밀번호가 맞으면 비밀번호가 변경")
    void changePasswordSuccess() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("oldpass12", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newpass34")).willReturn("encodedNew");

        memberService.updateMyInfo(1L, new MemberUpdateRequest("길동이", "010-1234-5678", "oldpass12", "newpass34"));

        log.info("[검증] changePasswordSuccess -> password={}", member.getPassword());
        assertThat(member.getPassword()).isEqualTo("encodedNew");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 예외 발생")
    void changePasswordWrongCurrentPassword() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrongpass", "encodedOld")).willReturn(false);

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
        						memberService.updateMyInfo(1L, new MemberUpdateRequest("길동이", "010-1234-5678", "wrongpass", "newpass34")));

        log.info("[검증] changePasswordWrongCurrentPassword -> exception={}", thrown.getMessage());
        assertThat(thrown).isInstanceOf(GeneralException.class);
        assertThat(((GeneralException) thrown).getCode()).isEqualTo(MemberErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외 발생")
    void memberNotFound() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
        						memberService.updateMyInfo(999L, new MemberUpdateRequest("길동이", "010-1234-5678", null, null)));

        log.info("[검증] memberNotFound -> exception={}", thrown.getMessage());
        assertThat(thrown).isInstanceOf(GeneralException.class);
        assertThat(((GeneralException) thrown).getCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}