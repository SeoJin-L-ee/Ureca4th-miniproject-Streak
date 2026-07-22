package com.example.member.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.member.converter.MemberConverter;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.UpdateMemberResDto;
import com.example.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public CustomResponse<MemberResDto> me(@CurrentUser MemberPrincipal principal) {
    	
    	//인증되지 않은 사용자는 401 처리
        return CustomResponse.onSuccess(memberService.getMyInfo(principal.memberId()));
    }

    @PatchMapping("/me")
    public CustomResponse<UpdateMemberResDto> updateMe(@CurrentUser MemberPrincipal principal,
												            @Valid @RequestBody UpdateMemberReqDto request,
												            HttpServletRequest servletRequest,
												            HttpServletResponse servletResponse,
												            Authentication authentication
												            )
    {
        MemberResDto response = memberService.updateMyInfo(principal.memberId(), request);

        //비밀번호 변경 시 재로그인
        boolean reLoginRequired = request.newPassword() != null;
        
        //세션 무효화 + SecurityContext 초기화 = 비밀번호 변경 시.
        if (reLoginRequired) {
            new SecurityContextLogoutHandler().logout(servletRequest, servletResponse, authentication);
        }

        return CustomResponse.onSuccess(MemberConverter.toMemberUpdateResDto(response, reLoginRequired));
    }
}