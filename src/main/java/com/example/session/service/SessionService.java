package com.example.session.service;

import com.example.global.common.CustomResponse;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.response.SessionDetailResDto;

public interface SessionService {
	CustomResponse<SessionDetailResDto> createSession(long studyId, long memberId, CreateSessionReqDto reqDto);
}
