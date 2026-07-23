package com.example.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.repository.ApplicationRepository;
import com.example.assignment.entity.Assignment;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.member.entity.Member;
import com.example.member.entity.enums.MemberStatus;
import com.example.mypage.dto.response.MyApplicationResDto;
import com.example.mypage.dto.response.MyAssignmentResDto;
import com.example.mypage.dto.response.MyAttendanceRateResDto;
import com.example.mypage.dto.response.MyPageDashboardResDto;
import com.example.mypage.dto.response.MyStreakResDto;
import com.example.mypage.dto.response.MyStudyResDto;
import com.example.mypage.dto.response.MyTodaySessionResDto;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class MyPageServiceTest {

    @Mock ParticipantRepository participantRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AttendanceService attendanceService;
    @Mock AssignmentRepository assignmentRepository;
    @Mock SessionRepository sessionRepository;
    @Mock ApplicationRepository applicationRepository;

    @InjectMocks MyPageServiceImpl myPageService;

    private Member member() {
        return Member.builder().id(1L).email("test@test.com").password("encodedOld").name("길동이").phone("010-1234-5678").status(MemberStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("참여 중인 스터디 목록을 조회한다")
    void getMyStudiesSuccess() {
        Study study = Study.builder().id(10L).title("그래프 탐색 스터디").category(StudyCategory.ALGORITHM).status(StudyStatus.RECRUITING).build();
        Participant participant = Participant.builder().id(1L).study(study).member(member()).role(StudyRole.LEADER).build();

        given(participantRepository.findAllByMemberIdFetchStudy(1L)).willReturn(List.of(participant));

        List<MyStudyResDto> result = myPageService.getMyStudies(1L);

        log.info("[검증] getMyStudiesSuccess -> {}", result);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).studyId()).isEqualTo(10L);
        assertThat(result.get(0).title()).isEqualTo("그래프 탐색 스터디");
        assertThat(result.get(0).role()).isEqualTo(StudyRole.LEADER);
    }

    @Test
    @DisplayName("출석 기록이 없으면 평균 출석률은 0")
    void getMyAttendanceRateEmpty() {
        given(attendanceRepository.countByMemberId(1L)).willReturn(0L);
        given(attendanceRepository.countByMemberIdAndStatus(1L, AttendanceStatus.PRESENT)).willReturn(0L);

        MyAttendanceRateResDto result = myPageService.getMyAttendanceRate(1L);

        log.info("[검증] getMyAttendanceRateEmpty -> {}", result);

        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.attendanceRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("4번 중 3번 출석하면 평균 출석률은 75%")
    void getMyAttendanceRateSuccess() {
        given(attendanceRepository.countByMemberId(1L)).willReturn(4L);
        given(attendanceRepository.countByMemberIdAndStatus(1L, AttendanceStatus.PRESENT)).willReturn(3L);

        MyAttendanceRateResDto result = myPageService.getMyAttendanceRate(1L);

        log.info("[검증] getMyAttendanceRateSuccess -> {}", result);
        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.presentCount()).isEqualTo(3);
        assertThat(result.attendanceRate()).isEqualTo(75);
    }

    @Test
    @DisplayName("최장 Streak 조회는 AttendanceService에 위임하고 결과를 그대로 감싸서 반환한다")
    void getMyLongestStreakDelegatesToAttendanceService() {
        given(attendanceService.getMyLongestStreak(1L)).willReturn(3);

        MyStreakResDto result = myPageService.getMyLongestStreak(1L);

        log.info("[검증] getMyLongestStreakDelegatesToAttendanceService -> {}", result);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 제출한 과제는 마감 기한 과제 목록에서 제외")
    void getMyDeadlineAssignmentsSuccess() {
        Study study = Study.builder().id(10L).title("영어 스터디").build();
        Session session = Session.builder().id(20L).study(study).title("1회차").build();
        Assignment notSubmitted = Assignment.builder().id(100L).session(session).title("과제A").dueAt(LocalDateTime.now().plusDays(3)).build();

        // 이미 제출한 과제(200L)는 쿼리 자체에서 걸러지므로, mock 응답에 아예 안 담아서 반환한다
        given(assignmentRepository.findUpcomingAndNotSubmittedByMemberId(any(), any())).willReturn(List.of(notSubmitted));

        List<MyAssignmentResDto> result = myPageService.getMyDeadlineAssignments(1L);

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

        List<MyTodaySessionResDto> result = myPageService.getMyTodaySessions(1L);

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

        List<MyApplicationResDto> result = myPageService.getMyApplications(1L, null);

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

        List<MyApplicationResDto> result = myPageService.getMyApplications(1L, ApplicationStatus.APPROVED);

        log.info("[검증] getMyApplicationsFilteredByStatus -> {}", result);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("마이페이지 대시보드는 6개 데이터를 한 번에 담아서 반환")
    void getMyPageDashboardSuccess() {
        Study study = Study.builder().id(10L).title("스터디").category(StudyCategory.ALGORITHM).status(StudyStatus.RECRUITING).build();
        Participant participant = Participant.builder().study(study).member(member()).role(StudyRole.LEADER).build();

        given(participantRepository.findAllByMemberIdFetchStudy(1L)).willReturn(List.of(participant));
        given(attendanceRepository.countByMemberId(1L)).willReturn(4L);
        given(attendanceRepository.countByMemberIdAndStatus(1L, AttendanceStatus.PRESENT)).willReturn(3L);
        given(attendanceService.getMyLongestStreak(1L)).willReturn(0);
        given(assignmentRepository.findUpcomingAndNotSubmittedByMemberId(any(), any())).willReturn(List.of());
        given(sessionRepository.findTodaySessionsByMemberId(any(), any(), any())).willReturn(List.of());
        given(applicationRepository.findAllByApplicantId(1L)).willReturn(List.of());

        MyPageDashboardResDto result = myPageService.getMyPageDashboard(1L);

        log.info("[검증] getMyPageDashboardSuccess -> {}", result);

        assertThat(result.studies()).hasSize(1);
        assertThat(result.studies().get(0).studyId()).isEqualTo(10L);
        assertThat(result.attendanceRate().attendanceRate()).isEqualTo(75);
        assertThat(result.streak().longestStreak()).isEqualTo(0);
        assertThat(result.deadlineAssignments()).isEmpty();
        assertThat(result.todaySessions()).isEmpty();
        assertThat(result.applications()).isEmpty();
    }
}
