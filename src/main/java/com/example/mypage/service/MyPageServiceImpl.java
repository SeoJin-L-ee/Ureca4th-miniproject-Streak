package com.example.mypage.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.repository.ApplicationRepository;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import com.example.mypage.converter.MyPageConverter;
import com.example.mypage.dto.response.MyApplicationResDto;
import com.example.mypage.dto.response.MyAssignmentResDto;
import com.example.mypage.dto.response.MyAttendanceRateResDto;
import com.example.mypage.dto.response.MyPageDashboardResDto;
import com.example.mypage.dto.response.MyStreakResDto;
import com.example.mypage.dto.response.MyStudyResDto;
import com.example.mypage.dto.response.MyTodaySessionResDto;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final ParticipantRepository participantRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    public List<MyStudyResDto> getMyStudies(Long memberId) {

        //내가 속한 모든 Participant를 Study와 함께 가져와서 DTO로 변환
        return participantRepository.findAllByMemberIdFetchStudy(memberId).stream().map(MyPageConverter::toMyStudyResDto).toList();
    }

    @Override
    public MyAttendanceRateResDto getMyAttendanceRate(Long memberId) {
        // 전체 개수와 PRESENT 개수를 DB에서 바로 COUNT로 세서 가져온다 (엔티티 목록을 메모리로 안 가져옴)
        long total = attendanceRepository.countByMemberId(memberId);
        long presentCount = attendanceRepository.countByMemberIdAndStatus(memberId, AttendanceStatus.PRESENT);

        int rate = total == 0 ? 0 : (int) Math.round((double) presentCount / total * 100);

        return new MyAttendanceRateResDto((int) total, (int) presentCount, rate);
    }

    @Override
    public MyStreakResDto getMyLongestStreak(Long memberId) {
        //연속 출석 계산은 출석(Attendance) 도메인의 규칙이라 AttendanceService에 위임
        return new MyStreakResDto(attendanceService.getMyLongestStreak(memberId));
    }

    @Override
    public List<MyAssignmentResDto> getMyDeadlineAssignments(Long memberId) {
        //참여 중인 스터디들의 마감 안 지난 과제 중, 이미 제출한 것은 쿼리에서 NOT EXISTS로 걸러서 반환
        return assignmentRepository.findUpcomingAndNotSubmittedByMemberId(memberId, LocalDateTime.now())
        							.stream()
					                .map(MyPageConverter::toMyAssignmentResDto)
					                .toList();
    }

    @Override
    public List<MyApplicationResDto> getMyApplications(Long memberId, ApplicationStatus status) {

    	//status가 없으면(null) 전체 신청 내역, 있으면 해당 상태(대기/승인/거절)로 필터링된 신청 내역 조회
        List<Application> applications = (status == null) ? applicationRepository.findAllByApplicantId(memberId) : applicationRepository.findAllByApplicantIdAndStatus(memberId, status);

        return applications.stream().map(MyPageConverter::toMyApplicationResDto).toList();
    }


    @Override
    public List<MyTodaySessionResDto> getMyTodaySessions(Long memberId) {

    	//오늘 하루에 해당하는 세션 조회
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Session> todaySessions = sessionRepository.findTodaySessionsByMemberId(memberId, start, end);

        return todaySessions.stream().map(MyPageConverter::toMyTodaySessionResDto).toList();
    }

    @Override
    public MyPageDashboardResDto getMyPageDashboard(Long memberId) {
        return new MyPageDashboardResDto(
						                getMyStudies(memberId),
						                getMyAttendanceRate(memberId),
						                getMyLongestStreak(memberId),
						                getMyDeadlineAssignments(memberId),
						                getMyTodaySessions(memberId),
						                getMyApplications(memberId, null)   //지원 현황은 필터 없이 전체
						                );
    }
}
