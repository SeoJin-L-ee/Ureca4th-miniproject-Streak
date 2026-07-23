package com.example.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.calendar.dto.response.CalendarItemResDto;
import com.example.calendar.dto.response.CalendarMonthResDto;
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

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class CalendarServiceImplTest {
	
	@Autowired
    private CalendarService calendarService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    private Member member;
    private Member otherMember;
    private Study study;
    
    @BeforeEach
    void setUp() {
    	 
        // 테스트용 회원 생성
        member = memberRepository.save(
                Member.builder()
                        .email("test1@example.com")
                        .name("테스터1")
                        .password("testpassword")
                        .phone("010-1234-5678")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        otherMember = memberRepository.save(
                Member.builder()
                        .email("other@example.com")
                        .name("다른회원1")
                        .password("otherpassword")
                        .phone("010-1234-5678")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        // 테스트용 스터디 및 참여 정보 생성
        study = studyRepository.save(
                Study.builder()
                        .title("알고리즘 스터디")
                        .description("설명")
                        .capacity(10)
                        .category(StudyCategory.ALGORITHM)
                        .status(StudyStatus.RECRUITING)
                        .build()
        );

        participantRepository.save(
                Participant.builder()
                        .study(study)
                        .member(member)
                        .role(StudyRole.LEADER)
                        .build()
        );
    }

    @Test
    @DisplayName("성공: 해당 월에 속한 스터디 회차 및 과제 일정을 모두 조회한다.")
    void getMonthSchedules_Success() {
    	
        // given - 2026-06-28 ~ 2026-08-01 
        YearMonth yearMonth = YearMonth.of(2026, 7);

        // 7월 내 스터디 회차 생성
        Session session = sessionRepository.save(
                Session.builder()
                        .study(study)
                        .sessionNumber(1)
                        .title("1회차 스터디")
                        .content("1회차 세션 내용")
                        .startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
                        .build()
        );

        // 7월 내 과제 생성
        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .session(session)
                        .title("1회차 백준 문제 풀이")
                        .description("1회차 문제 풀이는 이렇게 하시면 됩니다.")
                        .dueAt(LocalDateTime.of(2026, 7, 15, 23, 59))
                        .build()
        );

        // 8월 스터디 회차 (조회 대상에서 제외되어야 함)
        sessionRepository.save(
                Session.builder()
                        .study(study)
                        .sessionNumber(2)
                        .title("2회차 스터디")
                        .content("2회차 내용")
                        .startsAt(LocalDateTime.of(2026, 8, 19, 14, 0))
                        .build()
        );

        // when
        CalendarMonthResDto result = calendarService.getMonthSchedules(yearMonth, member.getId());

        // then
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(7);
        assertThat(result.schedules()).hasSize(2);

        // 회차 DTO 검증
        CalendarItemResDto sessionItem = result.schedules().stream()
                .filter(item -> "SESSION".equals(item.type()))
                .findFirst()
                .orElseThrow();

        assertThat(sessionItem.id()).isEqualTo(session.getId());
        assertThat(sessionItem.studyTitle()).isEqualTo("알고리즘 스터디");
        assertThat(sessionItem.title()).isEqualTo("1회차 스터디");
        assertThat(sessionItem.date()).isEqualTo(LocalDateTime.of(2026, 7, 10, 14, 0));

        // 과제 DTO 검증
        CalendarItemResDto assignmentItem = result.schedules().stream()
                .filter(item -> "ASSIGNMENT".equals(item.type()))
                .findFirst()
                .orElseThrow();

        assertThat(assignmentItem.id()).isEqualTo(assignment.getId());
        assertThat(assignmentItem.studyTitle()).isEqualTo("알고리즘 스터디");
        assertThat(assignmentItem.title()).isEqualTo("1회차 백준 문제 풀이");
        assertThat(assignmentItem.date()).isEqualTo(LocalDateTime.of(2026, 7, 15, 23, 59));
    }

    @Test
    @DisplayName("성공: 내가 참여하지 않은 스터디의 일정은 캘린더 조회 시 포함되지 않는다.")
    void getMonthSchedules_NotParticipant() {
    	
        // given
        YearMonth yearMonth = YearMonth.of(2026, 7);

        // otherMember만 참여하는 별도의 스터디 생성
        Study otherStudy = studyRepository.save(
                Study.builder()
                        .title("다른 스터디")
                        .description("다른 스터디 설명")
                        .capacity(10)
                        .category(StudyCategory.ALGORITHM)
                        .status(StudyStatus.RECRUITING)
                        .build()
        );

        participantRepository.save(
                Participant.builder()
                        .study(otherStudy)
                        .member(otherMember)
                        .role(StudyRole.LEADER)
                        .build()
        );

        sessionRepository.save(
                Session.builder()
                        .study(otherStudy)
                        .sessionNumber(1)
                        .title("다른 스터디 회차")
                        .content("다른 스터디 회차 내용")
                        .startsAt(LocalDateTime.of(2026, 7, 10, 14, 0))
                        .build()
        );

        // when (member ID로 조회)
        CalendarMonthResDto result = calendarService.getMonthSchedules(yearMonth, member.getId());

        // then
        assertThat(result.schedules()).isEmpty();
    }
    
}
