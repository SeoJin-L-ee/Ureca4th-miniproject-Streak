package com.example.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.example.global.common.CustomResponse;
import com.example.global.common.code.CommonErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberUserDetailsService memberUserDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 회원가입 시 암호화와 로그인 시 비밀번호 비교에 모두 사용한다.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    	
    	//로그인 이메일로 member 조회
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(memberUserDetailsService);

        //입력한 비밀번호와 DB의 BCrypt 해시값 비교
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider authenticationProvider) {
        //로그인 API가 호출할 인증 관리자
        return new ProviderManager(List.of(authenticationProvider));
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // ecurityContext를 서버 HttpSession에 저장
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {

    	//XSRF-TOKEN은 프론트가 헤더에 넣어야 하므로 JavaScript 접근 허용
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        		.securityContext(context -> context.securityContextRepository(securityContextRepository))
        			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        						.sessionFixation(fixation -> fixation.changeSessionId()))
        			.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.GET, "/", "/index.html", "/app.js", "/style.css", "/favicon.ico").permitAll()
        						.requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
    							.requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
    							.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
    							.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    								.anyRequest().authenticated())
    					.exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, ex) -> writeError(response, CommonErrorCode.UNAUTHORIZED))
    								.accessDeniedHandler((request, response, ex) -> writeError(response, CommonErrorCode.FORBIDDEN)))
    						.formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
    							.logout(logout -> logout.disable());
        
        return http.build();
    }

    private void writeError(HttpServletResponse response, CommonErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), CustomResponse.onFailure(errorCode));
    }
}