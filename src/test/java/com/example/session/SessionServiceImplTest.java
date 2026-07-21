package com.example.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.session.service.SessionServiceImpl;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.repository.StudyRepository;

import jakarta.transaction.Transactional;

@SpringBootTest 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SessionServiceImplTest {
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
    private MemberRepository memberRepository;
	
	@Autowired
	private ParticipantRepository participantRepository;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private SessionServiceImpl sessionService;
	
	private Member member;
    private Study study;
    private CreateSessionReqDto createReqDto;
    private UpdateSessionReqDto updateReqDto;
    
	@BeforeEach
    void setUp() {

        member = memberRepository.save(
                Member.builder()
                        .email("test@example.com")
                        .name("테스터")
                        .password("password123!")
                        .phone("010-1234-5678")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        study = studyRepository.save(
                Study.builder()
                        .creater(member)
                        .title("스터디 제목")
                        .description("설명")
                        .capacity(10)
                        .category(StudyCategory.ALGORITHM)
                        .status(StudyStatus.RECRUITING)
                        .build()
        );
        
        createReqDto = new CreateSessionReqDto(
                1,
                "테스트 세션1",
                "테스트 내용 1회차 입니다.",
                LocalDateTime.now()
        );
        
        updateReqDto = new UpdateSessionReqDto(
                1,
                "테스트 세션 수정1",
                "테스트 내용을 수정하였습니다.",
                LocalDateTime.now()
        );
    }
	
	@Test
	@Order(1)
	@DisplayName("스터디 회차 생성 - LEADER")
	void create_session() {
		
		// given
		participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(member)
                        .role(StudyRole.LEADER)
                        .build()
        );
		
		// when 
		SessionResDto response = sessionService.createSession(study.getId(), member.getId(), createReqDto);
		
		
		// then 
		// 응답 DTO 의 필드를 직접 검증 
	    assertThat(response.title()).isEqualTo("테스트 세션1");
	    assertThat(response.sessionNumber()).isEqualTo(1);
		
	    // DB 저장 상태 검증 
	    Session session = sessionRepository.findAll().get(0);

	    assertThat(session.getTitle()).isEqualTo("테스트 세션1");
		assertThat(session.getStudy().getId()).isEqualTo(study.getId());
		assertThat(session.getSessionNumber()).isEqualTo(1);
	}
	
	@Test 
	@Order(2)
	@DisplayName("스터디 회차 생성 실패 - LEADER 아닐 경우")
	void create_session_when_member() {

	    participantRepository.save(
	        Participant.builder()
	            .study(study)
	            .member(member)
	            .role(StudyRole.MEMBER)
	            .build()
	    );

	    assertThatThrownBy(() ->
	        sessionService.createSession(study.getId(), member.getId(), createReqDto)
	    )
	    .isInstanceOf(RuntimeException.class);
	}
	
	@Test
	@Order(3)
	@DisplayName("스터디 회차 수정 - LEADER")
	void update_session() {
		
		// given 
		participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(member)
                        .role(StudyRole.LEADER)
                        .build()
        );
		
		Session session = sessionRepository.save(
			    Session.builder()
			    		.study(study)
			    		.sessionNumber(1)
				        .title("기존 제목")
				        .content("기존 내용")
				        .startsAt(LocalDateTime.now())
				        .build()
		);
		
		// when 
		SessionResDto response = sessionService.updateSession(study.getId(), session.getId(), member.getId(), updateReqDto);
		
		// then
		// 응답 DTO 의 필드를 직접 검증 
		assertThat(response.title()).isEqualTo("테스트 세션 수정1");
		
		// DB 저장 상태 검증 
	    Session saved = sessionRepository.findAll().get(0);

	    assertThat(saved.getTitle()).isEqualTo("테스트 세션 수정1");
	    assertThat(saved.getContent()).isEqualTo("테스트 내용을 수정하였습니다.");
	}
	
	@Test 
	@Order(4)
	@DisplayName("스터디 회차 수정 실패 - LEADER 아닐 경우")
	void update_session_when_member() {
		
		// given 
	    participantRepository.save(
	    		Participant.builder()
		            	.study(study)
			            .member(member)
			            .role(StudyRole.MEMBER)
			            .build()
	    );
	    
	    Session session = sessionRepository.save(
			    Session.builder()
			    		.study(study)
			    		.sessionNumber(1)
				        .title("기존 제목")
				        .content("기존 내용")
				        .startsAt(LocalDateTime.now())
				        .build()
		);

	    assertThatThrownBy(() ->
	        sessionService.updateSession(study.getId(), session.getId(), member.getId(), updateReqDto)
	    )
	    .isInstanceOf(RuntimeException.class);
	}
	
	@Test
	@Order(5)
	@DisplayName("스터디 회차 삭제 - LEADER")
	void delete_session() {
		
		// given 
		participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(member)
                        .role(StudyRole.LEADER)
                        .build()
        );
		
		Session session = sessionRepository.save(
			    Session.builder()
			    		.study(study)
			    		.sessionNumber(1)
				        .title("기존 제목")
				        .content("기존 내용")
				        .startsAt(LocalDateTime.now())
				        .build()
		);
		
		// when 
		sessionService.deleteSession(study.getId(), session.getId(), member.getId());
		
		// then
		// DB에서 삭제되었는지 확인 
		boolean exists = sessionRepository.findById(session.getId()).isPresent();
		assertThat(exists).isFalse();
	}
	
	@Test 
	@Order(6)
	@DisplayName("스터디 회차 삭제 실패 - LEADER 아닐 경우")
	void delete_session_when_member() {
		
		// given 
	    participantRepository.save(
	    		Participant.builder()
		            	.study(study)
			            .member(member)
			            .role(StudyRole.MEMBER)
			            .build()
	    );
	    
	    Session session = sessionRepository.save(
			    Session.builder()
			    		.study(study)
			    		.sessionNumber(1)
				        .title("기존 제목")
				        .content("기존 내용")
				        .startsAt(LocalDateTime.now())
				        .build()
		);
	    
	    // when & then
	    assertThatThrownBy(() ->
	        sessionService.deleteSession(study.getId(), session.getId(), member.getId())
	    )
	    .isInstanceOf(RuntimeException.class);
	}
}	
