package com.example.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.auth.dto.request.LoginReqDto;
import com.example.auth.dto.request.SignUpReqDto;
import com.example.auth.dto.response.AuthResDto;
import com.example.auth.service.AuthService;
import com.example.global.security.MemberPrincipal;
import com.example.global.security.MemberUserDetailsService;
import com.example.global.security.SecurityConfig;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@Slf4j
public class AuthControllerSecurityTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@MockitoBean
    AuthService authService;
	
	@MockitoBean
	MemberUserDetailsService memberUserDetailsService;
	
	private MemberPrincipal principal() {
		return MemberPrincipal.from(Member.builder().id(1L).email("test@test.com").password("sadfgr").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build());
	}
	
	private void logResult(String label, MvcResult result) throws Exception {
		log.info("[검증] {} -> status={}, body={}", label, result.getResponse().getStatus(), result.getResponse().getContentAsString());
	}
	
	@Test
	@DisplayName("csrf 엔드포인트는 인증 없이 접근 가능")
	void csrfEndpointSuccess() throws Exception{
		MvcResult result = mockMvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
	    logResult("GET /api/auth/csrf", result);
	}
	
	@Test
	@DisplayName("signup에서 csrf토큰이 없으면 403 반환")
	void signupNotToken403() throws Exception{
		SignUpReqDto request = new SignUpReqDto("test@test.com", "aeds8945", "길동이", "010-1234-5678");
		
		MvcResult result = mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
																	.content(objectMapper.writeValueAsBytes(request)))
																	.andExpect(status().isForbidden()).andReturn();
	    logResult("POST /api/auth/signup (CSRF 없음)", result);
	}
	
	@Test
	@DisplayName("signup에서 csrf토큰이 있으면 인증없이 통과 O")
	void signupGoodTokenSuccess() throws Exception{
		SignUpReqDto request = new SignUpReqDto("test@test.com", "agbvc5415", "길동이", "010-1234-5678");
        given(authService.signUp(any())).willReturn(new AuthResDto(1L, "test@test.com", "길동이", "010-1234-5678"));

        MvcResult result = mockMvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
        																		.content(objectMapper.writeValueAsBytes(request)))
        																		.andExpect(status().isCreated()).andReturn();
        logResult("POST /api/auth/signup (CSRF 있음)", result);
    }

    @Test
    @DisplayName("login은 올바른 이메일/비밀번호면 200과 세션을 발급한다")
    void loginSuccess() throws Exception {
        Member member = Member.builder().id(1L).email("test@test.com").password(passwordEncoder.encode("abcd1234")).name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build();
        given(memberUserDetailsService.loadUserByUsername("test@test.com")).willReturn(MemberPrincipal.from(member));

        LoginReqDto request = new LoginReqDto("test@test.com", "abcd1234");

        MvcResult result = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
        															.content(objectMapper.writeValueAsBytes(request)))
        															.andExpect(status().isOk())
        															.andExpect(jsonPath("$.result.email").value("test@test.com"))
        															.andReturn();
        logResult("POST /api/auth/login (성공)", result);
    }

    @Test
    @DisplayName("login은 비밀번호가 틀리면 401을 반환한다")
    void loginWrongPassword401() throws Exception {
        Member member = Member.builder().id(1L).email("test@test.com").password(passwordEncoder.encode("abcd1234")).name("길동이").status(MemberStatus.ACTIVE).build();
        
        given(memberUserDetailsService.loadUserByUsername("test@test.com")).willReturn(MemberPrincipal.from(member));

        LoginReqDto request = new LoginReqDto("test@test.com", "wrongpass");

        MvcResult result = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
        															.content(objectMapper.writeValueAsBytes(request)))
        															.andExpect(status().isUnauthorized())
        															.andReturn();
        logResult("POST /api/auth/login (틀린 비밀번호)", result);
    }

    @Test
    @DisplayName("logout은 인증된 사용자의 요청이면 204를 반환한다")
    void logoutSuccess() throws Exception {
        MemberPrincipal principal = principal();

        MvcResult result = mockMvc.perform(post("/api/auth/logout").with(csrf())
        							.with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
        																		.andExpect(status().isNoContent()).andReturn();
        logResult("POST /api/auth/logout", result);
    }
}
