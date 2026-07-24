package com.example.session.service;

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

import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionInfoResDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.repository.StudyRepository;
import com.example.submission.entity.Submission;
import com.example.submission.repository.SubmissionRepository;

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
	private AssignmentRepository assignmentRepository;
	
	@Autowired
	private SubmissionRepository submissionRepository;
	
	@Autowired
	private AttendanceRepository attendanceRepository;
	
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
	
	@Test 
	@Order(7)
	@DisplayName("스터디 회차 상세 조회 성공")
	void detail_session_success() {

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
	            .title("1회차")
	            .content("내용")
	            .sessionNumber(1)
	            .startsAt(LocalDateTime.now())
	            .build()
	    );

	    // when
	    SessionInfoResDto result = sessionService.detailSession(study.getId(), session.getId(), member.getId());

	    // then
	    assertThat(result.title()).isEqualTo("1회차");
	    assertThat(result.sessionNumber()).isEqualTo(1);
	}
	
	@Test
	@Order(8)
	@DisplayName("스터디 회차 상세 조회 실패 - 참여하지 않은 유저")
	void detail_session_not_participant() {

	    Session session = sessionRepository.save(
	        Session.builder()
	            .study(study)
	            .title("1회차")
	            .content("내용")
	            .sessionNumber(1)
	            .startsAt(LocalDateTime.now())
	            .build()
	    );

	    assertThatThrownBy(() ->
	        sessionService.detailSession(study.getId(), session.getId(), member.getId())
	    )
	    .isInstanceOf(GeneralException.class);
	}
	
	@Test
	@Order(9)
	@DisplayName("스터디 회차 상세 조회 실패 - 잘못된 studyId")
	void detail_session_invalid_study() {

	    // 다른 스터디 생성
	    Study anotherStudy = studyRepository.save(
	        Study.builder()
	            .title("다른 스터디")
	            .description("설명")
	            .capacity(5)
	            .category(StudyCategory.ALGORITHM)
	            .status(StudyStatus.RECRUITING)
	            .build()
	    );

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
	            .title("1회차")
	            .sessionNumber(1)
	            .startsAt(LocalDateTime.now())
	            .build()
	    );

	    assertThatThrownBy(() ->
	        sessionService.detailSession(anotherStudy.getId(), session.getId(), member.getId())
	    )
	    .isInstanceOf(GeneralException.class);
	}
	
	@Test
	@Order(10)
	@DisplayName("스터디 회차 상세 조회 - 과제 제출 여부 포함")
	void detail_session_with_assignment_submission() {

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
	            .title("1회차")
	            .sessionNumber(1)
	            .startsAt(LocalDateTime.now())
	            .build()
	    );

	    Assignment assignment = assignmentRepository.save( 
	        Assignment.builder()
	            .session(session)
	            .title("과제1")
	            .description("과제 설명")
	            .dueAt(LocalDateTime.now())
	            .build()
	    );

	    // 제출 데이터 생성
	    submissionRepository.save(
	        Submission.builder()
	            .assignment(assignment)
	            .member(member)
	            .build()
	    );

	    // when
	    SessionInfoResDto result = sessionService.detailSession(study.getId(), session.getId(), member.getId());

	    // then
	    assertThat(result.assignments()).hasSize(1);
	    assertThat(result.assignments().get(0).isSubmitted()).isTrue();
	}
	
//	@Test
//	@Order(11)
//	@DisplayName("스터디 회차 목록 조회")
//	void session_list() {
//
//	    // given
//	    participantRepository.save(
//	        Participant.builder()
//	            .study(study)
//	            .member(member)
//	            .role(StudyRole.MEMBER)
//	            .build()
//	    );
//
//	    sessionRepository.save(
//	        Session.builder()
//	            .study(study)
//	            .title("1회차")
//	            .content("1회차 내용")
//	            .sessionNumber(1)
//	            .startsAt(LocalDateTime.now())
//	            .build()
//	    );
//
//	    sessionRepository.save(
//	        Session.builder()
//	            .study(study)
//	            .title("2회차")
//	            .content("2회차 내용")
//	            .sessionNumber(2)
//	            .startsAt(LocalDateTime.now().plusDays(1))
//	            .build()
//	    );
//
//
//	    // when
//	    List<SessionSummaryResDto> result = sessionService.listSession(study.getId(), member.getId());
//
//
//	    // then
//	    assertThat(result).hasSize(2);
//
//	    // Desc 정렬 검증
//	    assertThat(result.get(0).sessionNumber())
//	            .isEqualTo(2);
//
//	    assertThat(result.get(1).sessionNumber())
//	            .isEqualTo(1);
//
//
//	    // 데이터 매핑 검증
//	    assertThat(result.get(0).title())
//	            .isEqualTo("2회차");
//
//	    assertThat(result.get(1).title())
//	            .isEqualTo("1회차");
//	}
	
	@Test
	@Order(12)
	@DisplayName("스터디 회차 상세 조회 - 출석률 및 과제 제출률 계산 검증")
	void detail_session_with_rates() {

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
	            .title("1회차 세션")
	            .sessionNumber(1)
	            .startsAt(LocalDateTime.now())
	            .build()
	    );

	    // 출석 데이터 2개 생성 (1개 PRESENT, 1개 ABSENT -> 출석률 50%)
	    attendanceRepository.save(
	        Attendance.builder()
	            .session(session)
	            .member(member)
	            .status(AttendanceStatus.PRESENT)
	            .build()
	    );
	    
	    // 다른 회원의 결석 데이터
	    Member otherMember = memberRepository.save(
	        Member.builder()
	            .email("other@example.com")
	            .name("다른참여")
	            .password("password123!")
	            .phone("010-9999-8888")
	            .status(MemberStatus.ACTIVE)
	            .build()
	    );
	    
	    attendanceRepository.save(
	        Attendance.builder()
	            .session(session)
	            .member(otherMember)
	            .status(AttendanceStatus.ABSENT)
	            .build()
	    );

	    // 과제 2개 생성 및 1개만 제출 (제출률 50%)
	    Assignment assignment1 = assignmentRepository.save(
	        Assignment.builder()
	            .session(session)
	            .title("과제1")
	            .description("과제1 설명")
	            .dueAt(LocalDateTime.now().plusDays(1))
	            .build()
	    );

	    assignmentRepository.save(
	        Assignment.builder()
	            .session(session)
	            .title("과제2")
	            .description("과제2 설명")
	            .dueAt(LocalDateTime.now().plusDays(1))
	            .build()
	    );

	    // 과제1만 제출
	    submissionRepository.save(
	        Submission.builder()
	            .assignment(assignment1)
	            .member(member)
	            .build()
	    );

	    // when
	    SessionInfoResDto result = sessionService.detailSession(study.getId(), session.getId(), member.getId());

	    // then
	    // 출석률 50% (2명 중 1명 출석)
	    assertThat(result.attendanceRate()).isEqualTo(50);
	    
	    // 과제 제출률 50% (2개 과제 중 1개 제출)
	    assertThat(result.assignmentRate()).isEqualTo(50);
	}
}	
