package com.example.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.assignment.entity.Assignment;
import com.example.assignment.exception.AssignmentErrorCode;
import com.example.assignment.repository.AssignmentRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.repository.StudyRepository;
import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.request.UpdateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;
import com.example.submission.entity.Submission;
import com.example.submission.exception.SubmissionErrorCode;
import com.example.submission.repository.SubmissionRepository;

@SpringBootTest
@Transactional
public class SubmissionServiceImplTest {

	@Autowired
    private SubmissionService submissionService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Member member;
    private Member nonParticipantMember;
    private Study study;
    private Session session;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
    	
        // 회원 데이터 생성
        member = memberRepository.save(Member.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("password123")
                .phone("010-1111-1111")
                .status(MemberStatus.ACTIVE)
                .build());

        nonParticipantMember = memberRepository.save(Member.builder()
                .email("other@example.com")
                .name("외부인")
                .password("password123")
                .phone("010-2222-2222")
                .status(MemberStatus.ACTIVE)
                .build());

        
        // 스터디 및 회차 생성
        study = studyRepository.save(Study.builder()
                .title("알고리즘 스터디")
                .description("알고리즘 스터디입니다.")
                .capacity(10)
                .category(StudyCategory.ALGORITHM)
                .status(StudyStatus.RECRUITING)
                .build());
        

        session = sessionRepository.save(Session.builder()
                .study(study)
                .sessionNumber(1)
                .title("1주차 세션")
                .content("1주차 세션 내용")
                .startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
                .build());
        
        // 참여자 등록 (member만 스터디에 참여)
        participantRepository.save(Participant.builder()
                .study(study)
                .member(member)
                .role(StudyRole.LEADER)
                .build());


        // 과제 생성
        assignment = assignmentRepository.save(Assignment.builder()
                .session(session)
                .title("Spring JPA 과제")
                .description("과제에 대한 설명입니다.")
                .dueAt(LocalDateTime.of(2026, 7, 16, 23, 0))
                .build());

    }

    @Nested
    @DisplayName("과제 제출 (createSubmission)")
    class CreateSubmissionTest {

        @Test
        @DisplayName("성공: 올바른 요청일 경우 과제 제출이 저장되고 DTO가 반환된다.")
        void createSubmission_Success() {
        	
            // given
            CreateSubmissionReqDto reqDto = new CreateSubmissionReqDto("과제 제출 내용입니다.");

            // when
            SubmissionSummaryResDto response = submissionService.createSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    member.getId(),
                    reqDto
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.memberId()).isEqualTo(member.getId());
            assertThat(response.content()).isEqualTo("과제 제출 내용입니다.");

            // DB 저장 검증
            Submission savedSubmission = submissionRepository.findById(response.submissionId()).orElse(null);
            assertThat(savedSubmission).isNotNull();
            assertThat(savedSubmission.getAssignment().getId()).isEqualTo(assignment.getId());
        }

        @Test
        @DisplayName("예외: 스터디 참여자가 아닌 유저가 제출 시 예외가 발생한다.")
        void createSubmission_Forbidden_WhenNotParticipant() {
        	
            // given
            CreateSubmissionReqDto reqDto = new CreateSubmissionReqDto("외부인의 과제 제출");

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    nonParticipantMember.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(CommonErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 과제 ID 제출 시 예외가 발생한다.")
        void createSubmission_NotFound_WhenAssignmentNotExist() {
        	
            // given
            Long invalidAssignmentId = 9999L;
            CreateSubmissionReqDto reqDto = new CreateSubmissionReqDto("내용");

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(
                    study.getId(),
                    session.getId(),
                    invalidAssignmentId,
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("예외: 요청한 스터디의 과제가 아닐 경우 예외가 발생한다.")
        void createSubmission_BadRequest_WhenAssignmentNotInStudy() {
        	
            // given: 다른 스터디 및 과제 생성
            Study otherStudy = studyRepository.save(Study.builder()
            		.title("다른 스터디")
            		.description("다른 스터디 설명")
            		.capacity(5)
            		.category(StudyCategory.ALGORITHM)
            		.status(StudyStatus.RECRUITING)
            		.build()
            );
            
            Session otherSession = sessionRepository.save(Session.builder()
            		.study(otherStudy)
            		.sessionNumber(1)
            		.title("다른 세션")
            		.content("다른 세션 내용")
            		.startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
            		.build() 
            );
            
            Assignment otherAssignment = assignmentRepository.save(Assignment.builder()
            		.session(otherSession)
            		.title("다른 과제")
            		.description("다른 과제 설명")
            		.dueAt(LocalDateTime.of(2026, 7, 16, 23, 0))
            		.build()
            );

            // otherStudy 참여자로 설정
            participantRepository.save(Participant.builder()
            		.study(otherStudy)
            		.member(member)
            		.role(StudyRole.MEMBER)
            		.build()
            );

            CreateSubmissionReqDto reqDto = new CreateSubmissionReqDto("내용");

            // when & then: study.getId()에 otherAssignment.getId()를 요청
            assertThatThrownBy(() -> submissionService.createSubmission(
                    study.getId(),
                    session.getId(),
                    otherAssignment.getId(),
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(AssignmentErrorCode.NOT_STUDY_ASSIGNMENT);
        }

        @Test
        @DisplayName("예외: 이미 제출한 과제에 중복 제출 시 예외가 발생한다.")
        void createSubmission_Duplicate_WhenAlreadySubmitted() {
        	
            // given: 이미 제출 내역 존재
            submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(member)
                    .content("이전 제출 내용")
                    .build());

            CreateSubmissionReqDto reqDto = new CreateSubmissionReqDto("중복 제출 시도 내용");

            // when & then
            assertThatThrownBy(() -> submissionService.createSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(SubmissionErrorCode.DUPLICATE_SUBMISSION);
        }
    }
    
    
    
    @Nested
    @DisplayName("제출한 과제 수정 (updateSubmission)")
    class UpdateSubmissionTest {

        @Test
        @DisplayName("성공: 본인이 제출한 과제를 성공적으로 수정한다.")
        void updateSubmission_Success() {
        	
            // given
        	Submission submission = submissionRepository.save(Submission.builder()
            		.assignment(assignment)
            		.member(member)
            		.content("초기 과제 제출 내용")
            		.build());
            
            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("수정된 제출 내용입니다.");

            // when
            SubmissionSummaryResDto result = submissionService.updateSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    submission.getId(),
                    member.getId(),
                    reqDto
            );

            // then
            assertThat(result).isNotNull();
            
            // DB 변경사항 검증
            Submission updatedSubmission = submissionRepository.findById(submission.getId()).orElseThrow();
            assertThat(updatedSubmission.getContent()).isEqualTo("수정된 제출 내용입니다.");
        }

        @Test
        @DisplayName("예외: 스터디 참여자가 아닌 유저가 수정 시 예외가 발생한다.")
        void updateSubmission_Forbidden_WhenNotParticipant() {
        	
            // given
        	Submission submission = submissionRepository.save(Submission.builder()
            		.assignment(assignment)
            		.member(member)
            		.content("초기 과제 제출 내용")
            		.build());
        	
            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("외부인의 수정 시도");

            // when & then
            assertThatThrownBy(() -> submissionService.updateSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    submission.getId(),
                    nonParticipantMember.getId(), // 참여자가 아닌 유저
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(CommonErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 과제 ID일 경우 예외가 발생한다.")
        void updateSubmission_NotFound_WhenAssignmentNotExist() {
        	
            // given
        	Submission submission = submissionRepository.save(Submission.builder()
            		.assignment(assignment)
            		.member(member)
            		.content("초기 과제 제출 내용")
            		.build());
        	
            Long invalidAssignmentId = 9999L;
            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("수정 내용");

            // when & then
            assertThatThrownBy(() -> submissionService.updateSubmission(
                    study.getId(),
                    session.getId(),
                    invalidAssignmentId,
                    submission.getId(),
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("예외: 요청한 스터디의 과제가 아닐 경우 예외가 발생한다.")
        void updateSubmission_BadRequest_WhenAssignmentNotInStudy() {
        	
            // given: 다른 스터디 및 과제 생성
        	Submission submission = submissionRepository.save(Submission.builder()
            		.assignment(assignment)
            		.member(member)
            		.content("초기 과제 제출 내용")
            		.build());
        	
            Study otherStudy = studyRepository.save(Study.builder()
                    .title("다른 스터디")
                    .description("설명")
                    .capacity(5)
                    .category(StudyCategory.ALGORITHM)
                    .status(StudyStatus.RECRUITING)
                    .build());

            Session otherSession = sessionRepository.save(Session.builder()
                    .study(otherStudy)
                    .sessionNumber(1)
                    .title("다른 세션")
                    .content("내용")
                    .startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
                    .build());

            Assignment otherAssignment = assignmentRepository.save(Assignment.builder()
                    .session(otherSession)
                    .title("다른 과제")
                    .description("설명")
                    .dueAt(LocalDateTime.of(2026, 7, 16, 23, 0))
                    .build());

            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("수정 내용");

            // when & then
            assertThatThrownBy(() -> submissionService.updateSubmission(
                    study.getId(), // 현재 스터디
                    session.getId(),
                    otherAssignment.getId(), // 다른 스터디의 과제
                    submission.getId(),
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(AssignmentErrorCode.NOT_STUDY_ASSIGNMENT);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 제출물 ID일 경우 예외가 발생한다.")
        void updateSubmission_NotFound_WhenSubmissionNotExist() {
        	
            // given
            Long invalidSubmissionId = 9999L;
            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("수정 내용");

            // when & then
            assertThatThrownBy(() -> submissionService.updateSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    invalidSubmissionId,
                    member.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
        }

        @Test
        @DisplayName("예외: 본인이 작성한 제출물이 아닐 경우 예외가 발생한다.")
        void updateSubmission_Forbidden_WhenNotOwner() {
        	
            // given
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other_owner@example.com")
                    .name("다른참여자")
                    .password("password123")
                    .phone("010-4444-4444")
                    .status(MemberStatus.ACTIVE)
                    .build());

            participantRepository.save(Participant.builder()
                    .study(study)
                    .member(otherMember)
                    .role(StudyRole.MEMBER)
                    .build());

            Submission otherSubmission = submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(otherMember)
                    .content("다른 유저의 제출물")
                    .build());

            UpdateSubmissionReqDto reqDto = new UpdateSubmissionReqDto("남의 글 수정 시도");

            // when & then: member.getId()가 otherSubmission을 수정하려고 시도
            assertThatThrownBy(() -> submissionService.updateSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    otherSubmission.getId(),
                    member.getId(), // 작성자가 아닌 본인 ID로 요청
                    reqDto
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(SubmissionErrorCode.NOT_SUBMISSION_OWNER);
        }
    }
    
    
    @Nested
    @DisplayName("제출한 과제 삭제 (deleteSubmission)")
    class DeleteSubmissionTest {

        @Test
        @DisplayName("성공: 본인이 제출한 과제를 성공적으로 삭제한다.")
        void deleteSubmission_Success() {
        	
            // given
            Submission submission = submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(member)
                    .content("삭제할 과제 내용")
                    .build());

            // when
            submissionService.deleteSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    submission.getId(),
                    member.getId()
            );

            // then - DB에서 조회되지 않아야 함
            boolean exists = submissionRepository.existsById(submission.getId());
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("예외: 스터디 참여자가 아닌 유저가 삭제 시 예외가 발생한다.")
        void deleteSubmission_Forbidden_WhenNotParticipant() {
        	
            // given
            Submission submission = submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(member)
                    .content("삭제할 과제 내용")
                    .build());
            

            // when & then
            assertThatThrownBy(() -> submissionService.deleteSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    submission.getId(),
                    nonParticipantMember.getId()
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(CommonErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 과제 ID일 경우 예외가 발생한다.")
        void deleteSubmission_NotFound_WhenAssignmentNotExist() {
        	
            // given
            Submission submission = submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(member)
                    .content("삭제할 과제 내용")
                    .build());

            Long invalidAssignmentId = 9999L;

            // when & then
            assertThatThrownBy(() -> submissionService.deleteSubmission(
                    study.getId(),
                    session.getId(),
                    invalidAssignmentId,
                    submission.getId(),
                    member.getId()
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 제출물 ID일 경우 예외가 발생한다.")
        void deleteSubmission_NotFound_WhenSubmissionNotExist() {
        	
            // given
            Long invalidSubmissionId = 9999L;

            // when & then
            assertThatThrownBy(() -> submissionService.deleteSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    invalidSubmissionId,
                    member.getId()
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
        }

        @Test
        @DisplayName("예외: 본인이 작성한 제출물이 아닐 경우 예외가 발생한다.")
        void deleteSubmission_Forbidden_WhenNotOwner() {
        	
            // given: 다른 참여자의 제출물 생성
            Member otherMember = memberRepository.save(Member.builder()
                    .email("delete_owner@example.com")
                    .name("삭제작성자")
                    .password("password123")
                    .phone("010-5555-5555")
                    .status(MemberStatus.ACTIVE)
                    .build());

            participantRepository.save(Participant.builder()
                    .study(study)
                    .member(otherMember)
                    .role(StudyRole.MEMBER)
                    .build());

            Submission otherSubmission = submissionRepository.save(Submission.builder()
                    .assignment(assignment)
                    .member(otherMember)
                    .content("다른 유저의 제출물")
                    .build());

            // when & then: member.getId()가 다른 유저의 제출물을 삭제하려고 시도
            assertThatThrownBy(() -> submissionService.deleteSubmission(
                    study.getId(),
                    session.getId(),
                    assignment.getId(),
                    otherSubmission.getId(),
                    member.getId()
            ))
            .isInstanceOf(GeneralException.class)
            .extracting(e -> ((GeneralException) e).getCode())
            .isEqualTo(SubmissionErrorCode.NOT_SUBMISSION_OWNER);
        }
    }
}
