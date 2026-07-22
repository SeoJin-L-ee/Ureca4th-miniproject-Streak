package com.example.assignment.service;

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

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
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

@SpringBootTest
@Transactional
public class AssignmentServiceImplTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private Member leaderMember;
    private Member normalMember;
    private Study study;
    private Session session;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        leaderMember = memberRepository.save(
                Member.builder()
                        .email("leader@example.com")
                        .name("스터디장")
                        .password("password123")
                        .phone("010-1111-1111")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        normalMember = memberRepository.save(
                Member.builder()
                        .email("member@example.com")
                        .name("일반회원")
                        .password("password123")
                        .phone("010-2222-2222")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        // 테스트용 스터디 생성
        study = studyRepository.save(
                Study.builder()
                        .title("알고리즘 스터디")
                        .description("알고리즘 스터디입니다.")
                        .capacity(10)
                        .category(StudyCategory.ALGORITHM)
                        .status(StudyStatus.RECRUITING)
                        .build()
        );

        // 스터디 참여자 추가 
        participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(leaderMember)
                        .role(StudyRole.LEADER)
                        .build()
        );

        participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(normalMember)
                        .role(StudyRole.MEMBER)
                        .build()
        );

        // 테스트용 회차 생성
        session = sessionRepository.save(
                Session.builder()
                        .study(study)
                        .sessionNumber(1)
                        .title("1회차 DFS/BFS")
                        .content("세션 내용")
                        .startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
                        .build()
        );
    }

    @Nested
    @DisplayName("과제 생성 (createAssignment)")
    class CreateAssignment {

        @Test
        @DisplayName("성공: 스터디장이 요청할 경우 과제가 DB에 정상 저장된다.")
        void createAssignment_Success() {
        	
            // given
            CreateAssignmentReqDto reqDto = new CreateAssignmentReqDto(
                    "DFS 백준 3문제 풀이",
                    "1회차 세션 관련 문제 풀이 과제입니다.",
                    LocalDateTime.of(2026, 7, 15, 23, 59)
            );

            // when
            AssignmentInfoResDto response = assignmentService.createAssignment(
                    study.getId(),
                    session.getId(),
                    leaderMember.getId(),
                    reqDto
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.assignmentId()).isNotNull();
            assertThat(response.sessionId()).isEqualTo(session.getId());
            assertThat(response.title()).isEqualTo("DFS 백준 3문제 풀이");
            assertThat(response.description()).isEqualTo("1회차 세션 관련 문제 풀이 과제입니다.");
            assertThat(response.dueAt()).isEqualTo(LocalDateTime.of(2026, 7, 15, 23, 59));

            // DB 저장 검증
            Assignment savedAssignment = assignmentRepository.findById(response.assignmentId())
                    .orElseThrow();
            assertThat(savedAssignment.getTitle()).isEqualTo("DFS 백준 3문제 풀이");
            assertThat(savedAssignment.getSession().getId()).isEqualTo(session.getId());
        }

        @Test
        @DisplayName("실패: 스터디장이 아닌 일반 회원이 요청하면 예외가 발생한다.")
        void createAssignment_Fail_Forbidden() {
        	
            // given
            CreateAssignmentReqDto reqDto = new CreateAssignmentReqDto(
                    "과제 제목",
                    "과제 설명",
                    LocalDateTime.of(2026, 7, 15, 23, 59)
            );

            // when & then
            assertThatThrownBy(() -> assignmentService.createAssignment(
                    study.getId(),
                    session.getId(),
                    normalMember.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class);
        }

        @Test
        @DisplayName("실패: 존재하지 않거나 해당 스터디의 회차가 아닐 경우 예외가 발생한다.")
        void createAssignment_Fail_InvalidSession() {
        	
            // given
            Long invalidSessionId = 9999L;
            CreateAssignmentReqDto reqDto = new CreateAssignmentReqDto(
                    "과제 제목",
                    "과제 설명",
                    LocalDateTime.of(2026, 7, 15, 23, 59)
            );

            // when & then
            assertThatThrownBy(() -> assignmentService.createAssignment(
                    study.getId(),
                    invalidSessionId,
                    leaderMember.getId(),
                    reqDto
            ))
            .isInstanceOf(GeneralException.class);
        }
    }
}