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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.auth.dto.request.SignUpRequest;
import com.example.auth.dto.response.MemberResponse;
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
		SignUpRequest request = new SignUpRequest("test@test.com", "aeds8945", "길동이", "010-1234-5678");
		
		MvcResult result = mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
																	.content(objectMapper.writeValueAsBytes(request)))
																	.andExpect(status().isForbidden()).andReturn();
	    logResult("POST /api/auth/signup (CSRF 없음)", result);
	}
	
	@Test
	@DisplayName("signup에서 csrf토큰이 있으면 인증없이 통과 O")
	void signupGoodTokenSuccess() throws Exception{
		SignUpRequest request = new SignUpRequest("test@test.com", "agbvc5415", "길동이", "010-1234-5678");
        given(authService.signUp(any())).willReturn(new MemberResponse(1L, "test@test.com", "길동이", "010-1234-5678"));

        MvcResult result = mockMvc.perform(post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
        																		.content(objectMapper.writeValueAsBytes(request)))
        																		.andExpect(status().isCreated()).andReturn();
        logResult("POST /api/auth/signup (CSRF 있음)", result);
    }

    @Test
    @DisplayName("me는 인증되지 않은 요청이면 401 반환")
    void meNotAuthenticated401() throws Exception {
    	MvcResult result = mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andReturn();
        logResult("GET /api/auth/me (미인증)", result);
    }

    @Test
    @DisplayName("me는 인증된 요청이면 200과 회원 정보를 반환한다")
    void meGoodAuthenticatedReturn200andMember() throws Exception {
        MemberPrincipal principal = principal();

        MvcResult result = mockMvc.perform(get("/api/auth/me").with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))))
        														.andExpect(status().isOk()).andExpect(jsonPath("$.result.email").value("test@test.com")).andReturn();
        logResult("GET /api/auth/me (인증됨)", result);
    }
}
