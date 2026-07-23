package com.example.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.repository.ApplicationRepository;
import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.exception.GeneralException;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MyApplicationResDto;
import com.example.member.dto.response.MyAssignmentResDto;
import com.example.member.dto.response.MyAttendanceRateResDto;
import com.example.member.dto.response.MyStreakResDto;
import com.example.member.dto.response.MyStudyResDto;
import com.example.member.dto.response.MyTodaySessionResDto;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.submission.repository.SubmissionRepository;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;

    //마이페이지 조회에 필요한 Repository
    @Mock ParticipantRepository participantRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock SubmissionRepository submissionRepository;
    @Mock SessionRepository sessionRepository;
    @Mock ApplicationRepository applicationRepository;

    @InjectMocks MemberServiceImpl memberService;

    private Member member() {
        return Member.builder().id(1L).email("test@test.com").password("encodedOld").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("이름/전화번호만 수정하면 비밀번호는 그대로")
    void updateProfileOnly() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        memberService.updateMyInfo(1L, new UpdateMemberReqDto("변경이름", "010-9999-9999", null, null));

        log.info("[검증] updateProfileOnly -> name={}, phone={}, password={}", member.getName(), member.getPhone(), member.getPassword());

        assertThat(member.getName()).isEqualTo("변경이름");
        assertThat(member.getPhone()).isEqualTo("010-9999-9999");
        assertThat(member.getPassword()).isEqualTo("encodedOld");
    }

    @Test
    @DisplayName("현재 비밀번호가 맞으면 비밀번호가 변경")
    void changePasswordSuccess() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("oldpass12", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newpass34")).willReturn("encodedNew");

        memberService.updateMyInfo(1L, new UpdateMemberReqDto("길동이", "010-1234-5678", "oldpass12", "newpass34"));

        log.info("[검증] changePasswordSuccess -> password={}", member.getPassword());
        assertThat(member.getPassword()).isEqualTo("encodedNew");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 예외 발생")
    void changePasswordWrongCurrentPassword() {
        Member member = member();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrongpass", "encodedOld")).willReturn(false);

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
        						memberService.updateMyInfo(1L, new UpdateMemberReqDto("길동이", "010-1234-5678", "wrongpass", "newpass34")));

        log.info("[검증] changePasswordWrongCurrentPassword -> exception={}", thrown.getMessage());
        assertThat(thrown).isInstanceOf(GeneralException.class);
        assertThat(((GeneralException) thrown).getCode()).isEqualTo(MemberErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외 발생")
    void memberNotFound() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
        						memberService.updateMyInfo(999L, new UpdateMemberReqDto("길동이", "010-1234-5678", null, null)));

        log.info("[검증] memberNotFound -> exception={}", thrown.getMessage());
        assertThat(thrown).isInstanceOf(GeneralException.class);
        assertThat(((GeneralException) thrown).getCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("참여 중인 스터디 목록을 조회한다")
    void getMyStudiesSuccess() {
        Study study = Study.builder().id(10L).title("그래프 탐색 스터디").category(StudyCategory.ALGORITHM).status(StudyStatus.RECRUITING).build();
        Participant participant = Participant.builder().id(1L).study(study).member(member()).role(StudyRole.LEADER).build();

        given(participantRepository.findAllByMemberIdFetchStudy(1L)).willReturn(List.of(participant));

        List<MyStudyResDto> result = memberService.getMyStudies(1L);

        log.info("[검증] getMyStudiesSuccess -> {}", result);
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).studyId()).isEqualTo(10L);
        assertThat(result.get(0).title()).isEqualTo("그래프 탐색 스터디");
        assertThat(result.get(0).role()).isEqualTo(StudyRole.LEADER);
    }

    @Test
    @DisplayName("출석 기록이 없으면 평균 출석률은 0")
    void getMyAttendanceRateEmpty() {
        given(attendanceRepository.findAllByMemberIdOrderBySessionStartsAtAsc(1L)).willReturn(List.of());

        MyAttendanceRateResDto result = memberService.getMyAttendanceRate(1L);

        log.info("[검증] getMyAttendanceRateEmpty -> {}", result);
        
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.attendanceRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("4번 중 3번 출석하면 평균 출석률은 75%")
    void getMyAttendanceRateSuccess() {
        Attendance present1 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance present2 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance present3 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance absent = Attendance.builder().status(AttendanceStatus.ABSENT).build();

        given(attendanceRepository.findAllByMemberIdOrderBySessionStartsAtAsc(1L)).willReturn(List.of(present1, present2, present3, absent));

        MyAttendanceRateResDto result = memberService.getMyAttendanceRate(1L);

        log.info("[검증] getMyAttendanceRateSuccess -> {}", result);
        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.presentCount()).isEqualTo(3);
        assertThat(result.attendanceRate()).isEqualTo(75);
    }

    @Test
    @DisplayName("중간에 결석이 있으면 결석의 다음구간부터 체크하고 출석이 연속으로 찍히는 최장 구간(높은 숫자)만 반환")
    void getMyLongestStreakSuccess() {
        //PRESENT, PRESENT, ABSENT, PRESENT, PRESENT, PRESENT -> 최장 연속은 3
        Attendance p1 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance p2 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance absent = Attendance.builder().status(AttendanceStatus.ABSENT).build();
        Attendance p3 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance p4 = Attendance.builder().status(AttendanceStatus.PRESENT).build();
        Attendance p5 = Attendance.builder().status(AttendanceStatus.PRESENT).build();

        given(attendanceRepository.findAllByMemberIdOrderBySessionStartsAtAsc(1L)).willReturn(List.of(p1, p2, absent, p3, p4, p5));

        MyStreakResDto result = memberService.getMyLongestStreak(1L);

        log.info("[검증] getMyLongestStreakSuccess -> {}", result);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 제출한 과제는 마감 기한 과제 목록에서 제외")
    void getMyDeadlineAssignmentsSuccess() {
        Study study = Study.builder().id(10L).title("영어 스터디").build();
        Session session = Session.builder().id(20L).study(study).title("1회차").build();
        Assignment notSubmitted = Assignment.builder().id(100L).session(session).title("과제A").dueAt(LocalDateTime.now().plusDays(3)).build();
        Assignment submitted = Assignment.builder().id(200L).session(session).title("과제B").dueAt(LocalDateTime.now().plusDays(5)).build();

        given(assignmentRepository.findUpcomingByMemberId(any(), any())).willReturn(List.of(notSubmitted, submitted));
        given(submissionRepository.findSubmittedAssignmentIdsByMemberId(1L)).willReturn(List.of(200L));

        List<MyAssignmentResDto> result = memberService.getMyDeadlineAssignments(1L);

        log.info("[검증] getMyDeadlineAssignmentsSuccess -> {}", result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).assignmentId()).isEqualTo(100L);
        assertThat(result.get(0).title()).isEqualTo("과제A");
    }

    @Test
    @DisplayName("오늘 시작하는 회차 목록 조회")
    void getMyTodaySessionsSuccess() {
        Study study = Study.builder().id(10L).title("자격증 스터디").build();
        Session session = Session.builder().id(30L).study(study).title("3회차").startsAt(LocalDateTime.now()).build();

        given(sessionRepository.findTodaySessionsByMemberId(any(), any(), any())).willReturn(List.of(session));

        List<MyTodaySessionResDto> result = memberService.getMyTodaySessions(1L);

        log.info("[검증] getMyTodaySessionsSuccess -> {}", result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).sessionId()).isEqualTo(30L);
        assertThat(result.get(0).studyTitle()).isEqualTo("자격증 스터디");
    }

    @Test
    @DisplayName("status가 없으면 전체 지원 현황 조회")
    void getMyApplicationsAllStatus() {
        Study study = Study.builder().id(10L).title("영어 스터디").build();
        Application application = Application.builder().id(1L).study(study).status(ApplicationStatus.PENDING).build();

        given(applicationRepository.findAllByApplicantId(1L)).willReturn(List.of(application));

        List<MyApplicationResDto> result = memberService.getMyApplications(1L, null);

        log.info("[검증] getMyApplicationsAllStatus -> {}", result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("status가 있으면 '대기/승인/거절' 상태의 지원 현황만 조회")
    void getMyApplicationsFilteredByStatus() {
        Study study = Study.builder().id(10L).title("영어 스터디").build();
        Application application = Application.builder().id(2L).study(study).status(ApplicationStatus.APPROVED).build();

        given(applicationRepository.findAllByApplicantIdAndStatus(1L, ApplicationStatus.APPROVED)).willReturn(List.of(application));

        List<MyApplicationResDto> result = memberService.getMyApplications(1L, ApplicationStatus.APPROVED);

        log.info("[검증] getMyApplicationsFilteredByStatus -> {}", result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ApplicationStatus.APPROVED);
    }
}