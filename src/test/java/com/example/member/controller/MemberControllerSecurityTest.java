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

import com.example.application.entity.enums.ApplicationStatus;
import com.example.global.security.MemberPrincipal;
import com.example.global.security.MemberUserDetailsService;
import com.example.global.security.SecurityConfig;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.MyApplicationResDto;
import com.example.member.dto.response.MyAssignmentResDto;
import com.example.member.dto.response.MyAttendanceRateResDto;
import com.example.member.dto.response.MyStreakResDto;
import com.example.member.dto.response.MyStudyResDto;
import com.example.member.dto.response.MyTodaySessionResDto;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.service.MemberService;
import com.example.participant.entity.enums.StudyRole;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

import java.time.LocalDateTime;
import java.util.List;

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
        
        given(memberService.getMyInfo(1L)).willReturn(new MemberResDto(1L, "test@test.com", "길동이", "010-1234-5678"));

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
        UpdateMemberReqDto request = new UpdateMemberReqDto("변경이름", "010-9999-9999", null, null);

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
        UpdateMemberReqDto request = new UpdateMemberReqDto("변경이름", "010-9999-9999", null, null);
        given(memberService.updateMyInfo(eq(1L), any())).willReturn(new MemberResDto(1L, "test@test.com", "변경이름", "010-9999-9999"));

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
    @DisplayName("비밀번호 변경이 성공하면 reLoginRequired는 true이고 세션은 무효화")
    void updatePasswordSuccessInvalidatesSession() throws Exception {
        MemberPrincipal p = principal();
        UpdateMemberReqDto request = new UpdateMemberReqDto("길동이", "010-1234-5678", "oldpass12", "newpass34");
        
        given(memberService.updateMyInfo(eq(1L), any())).willReturn(new MemberResDto(1L, "test@test.com", "길동이", "010-1234-5678"));

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
        UpdateMemberReqDto request = new UpdateMemberReqDto("", "010-9999-9999", null, null);

        MvcResult result = mockMvc.perform(patch("/api/members/me").with(csrf())
											                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities())))
											                        .contentType(MediaType.APPLICATION_JSON)
											                        .content(objectMapper.writeValueAsBytes(request)))
                					.andExpect(status().isBadRequest()).andReturn();
        logResult("PATCH /api/members/me (이름 빈값)", result);
    }

    @Test
    @DisplayName("마이페이지 API는 미인증이면 401 (studies로 대표 확인)")
    void myPageNotAuthenticated401() throws Exception {
        // SecurityConfig의 anyRequest().authenticated()는 /api/members/me/** 전체에 동일하게 적용되므로
        // 하나의 엔드포인트로만 검증해도 나머지 5개에도 같은 규칙이 적용된다는 걸 알 수 있다.
        MvcResult result = mockMvc.perform(get("/api/members/me/studies")).andExpect(status().isUnauthorized()).andReturn();
        logResult("GET /api/members/me/studies (미인증)", result);
    }

    @Test
    @DisplayName("참여 중인 스터디 목록 조회 성공")
    void myStudiesSuccess() throws Exception {
        MemberPrincipal p = principal();
        
        MyStudyResDto studyDto = new MyStudyResDto(10L, "알고리즘 스터디", StudyCategory.ALGORITHM, StudyStatus.RECRUITING, StudyRole.LEADER);
        given(memberService.getMyStudies(1L)).willReturn(List.of(studyDto));

        MvcResult result = mockMvc.perform(get("/api/members/me/studies").with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
															                .andExpect(status().isOk())
															                .andExpect(jsonPath("$.result[0].studyId").value(10L))
															                .andExpect(jsonPath("$.result[0].role").value("LEADER"))
															                .andReturn();
        logResult("GET /api/members/me/studies", result);
    }

    @Test
    @DisplayName("평균 출석률 조회 성공")
    void myAttendanceRateSuccess() throws Exception {
        MemberPrincipal p = principal();
        given(memberService.getMyAttendanceRate(1L)).willReturn(new MyAttendanceRateResDto(4, 3, 75));

        MvcResult result = mockMvc.perform(get("/api/members/me/attendance")
					        		.with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result.attendanceRate").value(75))
					                .andReturn();
        logResult("GET /api/members/me/attendance", result);
    }

    @Test
    @DisplayName("최장 Streak 조회 성공")
    void myLongestStreakSuccess() throws Exception {
        MemberPrincipal p = principal();
        given(memberService.getMyLongestStreak(1L)).willReturn(new MyStreakResDto(3));

        MvcResult result = mockMvc.perform(get("/api/members/me/attendance/streak")
			                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result.longestStreak").value(3))
					                .andReturn();
        logResult("GET /api/members/me/attendance/streak", result);
    }

    @Test
    @DisplayName("마감 기한 과제 조회 성공")
    void myDeadlineAssignmentsSuccess() throws Exception {
        MemberPrincipal p = principal();
        MyAssignmentResDto assignmentDto = new MyAssignmentResDto(100L, 10L, "영어 스터디", "과제A", LocalDateTime.now().plusDays(3));
        
        given(memberService.getMyDeadlineAssignments(1L)).willReturn(List.of(assignmentDto));

        MvcResult result = mockMvc.perform(get("/api/members/me/assignments")
			                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result[0].title").value("과제A"))
					                .andReturn();
        logResult("GET /api/members/me/assignments", result);
    }

    @Test
    @DisplayName("스터디 지원 현황 조회 성공 - status 파라미터 없이 전체 조회")
    void myApplicationsAllStatusSuccess() throws Exception {
        MemberPrincipal p = principal();
        MyApplicationResDto applicationDto = new MyApplicationResDto(1L, 10L, "영어 스터디", ApplicationStatus.PENDING);
        given(memberService.getMyApplications(1L, null)).willReturn(List.of(applicationDto));

        MvcResult result = mockMvc.perform(get("/api/members/me/applications")
			                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result[0].status").value("PENDING"))
					                .andReturn();
        logResult("GET /api/members/me/applications (전체)", result);
    }

    @Test
    @DisplayName("스터디 지원 현황 조회 성공 - status=APPROVED로 필터링")
    void myApplicationsFilteredByStatusSuccess() throws Exception {
        MemberPrincipal p = principal();
        MyApplicationResDto applicationDto = new MyApplicationResDto(2L, 10L, "영어 스터디", ApplicationStatus.APPROVED);
        given(memberService.getMyApplications(1L, ApplicationStatus.APPROVED)).willReturn(List.of(applicationDto));

        MvcResult result = mockMvc.perform(get("/api/members/me/applications").queryParam("status", "APPROVED")
			                        	.with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result[0].status").value("APPROVED"))
					                .andReturn();
        logResult("GET /api/members/me/applications?status=APPROVED", result);
    }
    
    @Test
    @DisplayName("오늘 회차 조회 성공")
    void myTodaySessionsSuccess() throws Exception {
        MemberPrincipal p = principal();
        MyTodaySessionResDto sessionDto = new MyTodaySessionResDto(30L, 10L, "자격증 스터디", "3회차", LocalDateTime.now());
        given(memberService.getMyTodaySessions(1L)).willReturn(List.of(sessionDto));

        MvcResult result = mockMvc.perform(get("/api/members/me/schedule/today")
			                        .with(authentication(new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()))))
					                .andExpect(status().isOk())
					                .andExpect(jsonPath("$.result[0].sessionId").value(30L))
					                .andReturn();
        logResult("GET /api/members/me/schedule/today", result);
    }
}