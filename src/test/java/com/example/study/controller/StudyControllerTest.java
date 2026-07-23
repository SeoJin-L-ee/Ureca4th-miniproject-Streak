package com.example.study.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.global.security.MemberPrincipal;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.service.StudyCommandService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(StudyController.class)
public class StudyControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudyCommandService studyCommandService;
    
    // 시큐리티 인증 완료한 가짜 사용자 정보 생성
    private Authentication getAuthentication() {
        MemberPrincipal principal = MemberPrincipal.from(
                Member.builder()
                        .id(1L)
                        .email("test@gmail.com")
                        .password("1234")
                        .name("이서진")
                        .phone("010-1234-5678")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
    
    @Test
    @DisplayName("스터디 생성 (POST /api/studies)")
    void createStudy() throws Exception {
    	CreateStudyReqDto reqDto = new CreateStudyReqDto("유레카 스터디", "설명", 5, StudyCategory.ALGORITHM);
        StudyInfoResDto resDto = new StudyInfoResDto("유레카 스터디", "설명", 5, null, StudyStatus.RECRUITING);
        // Controller 테스트이므로, 서비스 로직이 잘 작동한다고 가정하고 만들어 둔 resDto 반환
        given(studyCommandService.createStudy(any(), any(CreateStudyReqDto.class))).willReturn(resDto);

        this.mockMvc.perform(
        		post("/api/studies")
        			.with(csrf())
        			.with(authentication(getAuthentication()))
        			.contentType(MediaType.APPLICATION_JSON)
        			.content(objectMapper.writeValueAsString(reqDto))
        )
        .andExpect(status().isOk());
    }
 
    @Test
    @DisplayName("스터디 수정 (PATCH /api/studies/{studyId})")
    void updateStudy() throws Exception {
        UpdateStudyReqDto reqDto = new UpdateStudyReqDto("수정된 제목", "수정된 설명", 5, null);
        StudyInfoResDto resDto = new StudyInfoResDto("수정된 제목", "수정된 설명", 5, null, StudyStatus.RECRUITING);
        given(studyCommandService.updateStudy(any(), eq(1L), any(UpdateStudyReqDto.class))).willReturn(resDto);

        this.mockMvc.perform(
        		patch("/api/studies/{studyId}", 1L)
                	.with(csrf())
                	.with(authentication(getAuthentication()))
                	.contentType(MediaType.APPLICATION_JSON)
                	.content(objectMapper.writeValueAsString(reqDto))
        )
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("스터디 상태 변경 (PATCH /api/studies/{studyId}/status)")
    void updateStudyStatus() throws Exception {
        StudyInfoResDto resDto = new StudyInfoResDto("제목", "설명", 5, null, StudyStatus.CLOSED);
        given(studyCommandService.updateStudyStatus(any(), eq(1L), eq(StudyStatus.CLOSED))).willReturn(resDto);

        this.mockMvc.perform(
        		patch("/api/studies/{studyId}/status", 1L)
        			.with(csrf())
        			.with(authentication(getAuthentication()))
        			.param("status", "CLOSED")
        )
        .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("스터디장 변경 (PATCH /api/studies/{studyId}/leader)")
    void updateStudyLeader() throws Exception {
        UpdateStudyLeaderResDto resDto = new UpdateStudyLeaderResDto(1L, 1L, "새 스터디장 이름");
        given(studyCommandService.updateStudyLeader(any(), eq(1L), eq(1L))).willReturn(resDto);
        
        this.mockMvc.perform(
        		patch("/api/studies/{studyId}/leader", 1L)
        			.with(csrf())
        			.with(authentication(getAuthentication()))
        			.param("newLeaderId", String.valueOf(1L))
        )
        .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("스터디 soft delete (DELETE /api/studies/{studyId})")
    void softDeleteStudy() throws Exception {
        doNothing().when(studyCommandService).softDeleteStudy(any(), eq(1L));

        this.mockMvc.perform(
        		delete("/api/studies/{studyId}", 1L)
        			.with(csrf())
        			.with(authentication(getAuthentication()))
        )
        .andExpect(status().isOk());
    }
    
}
