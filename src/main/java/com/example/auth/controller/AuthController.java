package com.example.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.SignUpRequest;
import com.example.auth.dto.response.CsrfResponse;
import com.example.auth.dto.response.MemberResponse;
import com.example.auth.service.AuthService;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/csrf")
    public CustomResponse<CsrfResponse> csrf(CsrfToken csrfToken) {
        //XSRF-TOKEN 발급
        return CustomResponse.onSuccess(new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName()));
    }

    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = authService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.onSuccess(HttpStatus.CREATED, response));
    }

    @PostMapping("/login")
    public CustomResponse<MemberResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        
    	//이메일과 비밀번호 검증
        Authentication authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword()));

        //인증된 사용자 정보 저장
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        //SecurityContext를 HttpSession에 저장
        //브라우저에는 사용자 정보가 아닌 JSESSIONID 쿠키만 전달
        securityContextRepository.saveContext(securityContext, servletRequest, servletResponse);

        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();

        return CustomResponse.onSuccess(new MemberResponse(principal.getMemberId(), principal.getEmail(), principal.getName(), principal.getPhone()));
    }

    @GetMapping("/me")
    public CustomResponse<MemberResponse> me(@CurrentUser MemberPrincipal principal) {
        //인증되지 않은 사용자는 SecurityConfig에서 401 처리
        return CustomResponse.onSuccess(new MemberResponse(principal.getMemberId(), principal.getEmail(), principal.getName(), principal.getPhone()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    	new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}