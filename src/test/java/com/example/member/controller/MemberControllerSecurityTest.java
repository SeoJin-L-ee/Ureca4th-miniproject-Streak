package com.example.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.auth.dto.response.MemberResponse;
import com.example.global.security.MemberPrincipal;
import com.example.global.security.MemberUserDetailsService;
import com.example.global.security.SecurityConfig;
import com.example.member.dto.request.MemberUpdateRequest;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.service.MemberService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MemberController.class)
@Import(SecurityConfig.class)
@Slf4j
public class MemberControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean MemberService memberService;
    @MockitoBean MemberUserDetailsService memberUserDetailsService;

    private MemberPrincipal principal() {
        return MemberPrincipal.from(Member.builder().id(1L).email("test@test.com").password("encoded").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build());
    }

    private void logResult(String label, MvcResult result) throws Exception {
        log.info("[검증] {} -> status={}, body={}", label, result.getResponse().getStatus(), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("me 조회는 미인증이면 401")
    void meNotAuthenticated401() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/members/me")).andExpect(status().isUnauthorized()).andReturn();
        logResult("GET /api/members/me (미인증)", result);
    }

    @Test
    @DisplayName("me 조회는 인증되면 200과 정보 반환")
    void meAuthenticated200() throws Exception {
        MemberPrincipal p = principal();
        
        given(memberService.getMyInfo(1L)).willReturn(new MemberResponse(1L, "test@test.com", "길동이", "010-1234-5678"));

        MvcResult result = mockMvc.perform(get("/api/members/me").with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
													                .andExpect(status().isOk())
													                .andExpect(jsonPath("$.result.name").value("길동이"))
													                .andReturn();
        logResult("GET /api/members/me (인증됨)", result);
    }

    @Test
    @DisplayName("me 수정은 csrf 없으면 403")
    void updateNoCsrf403() throws Exception {
        MemberPrincipal p = principal();
        MemberUpdateRequest request = new MemberUpdateRequest("변경이름", "010-9999-9999", null, null);

        MvcResult result = mockMvc.perform(patch("/api/members/me").with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities())))
											                        .contentType(MediaType.APPLICATION_JSON)
											                        .content(objectMapper.writeValueAsBytes(request)))
                													.andExpect(status().isForbidden()).andReturn();
        logResult("PATCH /api/members/me (CSRF 없음)", result);
    }

    @Test
    @DisplayName("이름/전화번호만 바꾸면 200, reLoginRequired는 false, 세션 유지")
    void updateProfileOnlySuccess() throws Exception {
        MemberPrincipal p = principal();
        MemberUpdateRequest request = new MemberUpdateRequest("변경이름", "010-9999-9999", null, null);
        given(memberService.updateMyInfo(eq(1L), any())).willReturn(new MemberResponse(1L, "test@test.com", "변경이름", "010-9999-9999"));

        MockHttpSession session = new MockHttpSession();

        MvcResult result = mockMvc.perform(patch("/api/members/me").with(csrf())
											                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities())))
											                        .session(session)
											                        .contentType(MediaType.APPLICATION_JSON)
											                        .content(objectMapper.writeValueAsBytes(request)))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result.member.name").value("변경이름"))
					                .andExpect(jsonPath("$.result.reLoginRequired").value(false))
					                .andReturn();
        logResult("PATCH /api/members/me (이름/전화번호만 변경)", result);

        log.info("[검증] 세션 무효화 여부 -> isInvalid={}", session.isInvalid());
        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    @DisplayName("비밀번호 변경이 성공하면 reLoginRequired는 true이고 세션이 무효화된다")
    void updatePasswordSuccessInvalidatesSession() throws Exception {
        MemberPrincipal p = principal();
        MemberUpdateRequest request = new MemberUpdateRequest("길동이", "010-1234-5678", "oldpass12", "newpass34");
        
        given(memberService.updateMyInfo(eq(1L), any())).willReturn(new MemberResponse(1L, "test@test.com", "길동이", "010-1234-5678"));

        MockHttpSession session = new MockHttpSession();

        MvcResult result = mockMvc.perform(patch("/api/members/me").with(csrf())
											                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities())))
											                        .session(session)
											                        .contentType(MediaType.APPLICATION_JSON)
											                        .content(objectMapper.writeValueAsBytes(request)))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result.reLoginRequired").value(true))
					                .andReturn();
        logResult("PATCH /api/members/me (비밀번호 변경)", result);

        log.info("[검증] 세션 무효화 여부 -> isInvalid={}", session.isInvalid());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("이름이 빈 값이면 400")
    void updateBlankName400() throws Exception {
        MemberPrincipal p = principal();
        MemberUpdateRequest request = new MemberUpdateRequest("", "010-9999-9999", null, null);

        MvcResult result = mockMvc.perform(patch("/api/members/me").with(csrf())
											                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities())))
											                        .contentType(MediaType.APPLICATION_JSON)
											                        .content(objectMapper.writeValueAsBytes(request)))
                					.andExpect(status().isBadRequest()).andReturn();
        logResult("PATCH /api/members/me (이름 빈값)", result);
    }
}