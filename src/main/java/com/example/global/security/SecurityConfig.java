package com.example.global.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.global.common.CustomResponse;
import com.example.global.common.code.CommonErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	
	@Bean
	PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // 프론트 연동 단순화를 위해 일단 csrf 비활성화 (변경 가능성 고려해서 람다식으로 작성)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(request -> request
            	.requestMatchers(
            			HttpMethod.POST,
            			"/api/auth/signup",
            			"/api/auth/login"
            	).permitAll()
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // 프론트가 POST 요청 보낼 로그아웃 주소
                .invalidateHttpSession(true) // 서버 세션 무효화
                .clearAuthentication(true) // 세션에 남아있는 인증정보 삭제
                .deleteCookies("JSESSIONID") // 브라우저 쿠키 삭제
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json;charset=UTF-8");
                    CustomResponse<String> body = CustomResponse.onSuccess("로그아웃 성공");
                    objectMapper.writeValue(response.getWriter(), body);
                })
            )
            .exceptionHandling(exception -> exception
            	// 비로그인 접근은 401
            	.authenticationEntryPoint((request, response, authException) -> {
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		response.setContentType("application/json;charset=UTF-8");
            		CustomResponse<Object> body =CustomResponse.onFailure(CommonErrorCode.UNAUTHORIZED);
            		objectMapper.writeValue(response.getWriter(), body);
            	})
            	// 권한 부족은 403
            	.accessDeniedHandler((request, response, accessDeniedException) -> {
            		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            		response.setContentType("application/json;charset=UTF-8");
            		CustomResponse<Object> body = CustomResponse.onFailure(CommonErrorCode.FORBIDDEN);
            		objectMapper.writeValue(response.getWriter(), body);
            	})
            );
        return http.build();
    }

    // CORS 상세 설정 빈(Bean)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // 세션 쿠키(JSESSIONID)를 주고 받을 수 있도록 허용
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 규칙 적용
        return source;
    }
}
