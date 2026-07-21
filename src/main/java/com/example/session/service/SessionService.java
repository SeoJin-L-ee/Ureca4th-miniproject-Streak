package com.example.session.service;

import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionInfoResDto;
import com.example.session.dto.response.SessionResDto;

public interface SessionService {
	
	// 스터디 회차 생성  
	SessionResDto createSession(long studyId, long memberId, CreateSessionReqDto reqDto);
	
	// 스터디 회차 수정 
	SessionResDto updateSession(long studyId, long sessionId, long memberId, UpdateSessionReqDto reqDto);
	
	// 스터디 회차 삭제 
	void deleteSession(long studyId, long sessionId, long memberId);
	
	// 스터디 회차 상세 조회 
	SessionInfoResDto detailSession(long studyId, long sessionId, long memberId);
}
