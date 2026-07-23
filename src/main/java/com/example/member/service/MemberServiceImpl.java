package com.example.member.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.repository.ApplicationRepository;
import com.example.assignment.repository.AssignmentRepository;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.attendance.repository.AttendanceRepository;
import com.example.global.common.exception.GeneralException;
import com.example.member.converter.MemberConverter;
import com.example.member.dto.request.UpdateMemberReqDto;
import com.example.member.dto.response.MemberResDto;
import com.example.member.dto.response.MyApplicationResDto;
import com.example.member.dto.response.MyAssignmentResDto;
import com.example.member.dto.response.MyAttendanceRateResDto;
import com.example.member.dto.response.MyStreakResDto;
import com.example.member.dto.response.MyStudyResDto;
import com.example.member.dto.response.MyTodaySessionResDto;
import com.example.member.entity.Member;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.submission.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    //마이페이지 조회에 필요한 Repository
    private final ParticipantRepository participantRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final SessionRepository sessionRepository;
    private final ApplicationRepository applicationRepository;

    //@CurrentUser로 받은 memberId는 이미 인증된 세션의 값이라 이론상 항상 존재하지만, 방어적으로 예외 처리
    @Override
    public MemberResDto getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberConverter.toMemberResponse(member);
    }

    @Override
    @Transactional
    public MemberResDto updateMyInfo(Long memberId, UpdateMemberReqDto request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        //이름/전화번호는 항상 수정 대상 (필수 입력)
        member.updateProfile(request.name(), request.phone());

        //newPassword가 있을 때만 비밀번호 변경
        if (request.newPassword() != null) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
                throw new GeneralException(MemberErrorCode.PASSWORD_MISMATCH);
            }
            member.changePassword(passwordEncoder.encode(request.newPassword()));
        }

        return MemberConverter.toMemberResponse(member);
    }

    @Override
    public List<MyStudyResDto> getMyStudies(Long memberId) {
    	
        //내가 속한 모든 Participant를 Study와 함께 가져와서 DTO로 변환
        return participantRepository.findAllByMemberIdFetchStudy(memberId).stream().map(MemberConverter::toMyStudyResDto).toList();
    }

    @Override
    public MyAttendanceRateResDto getMyAttendanceRate(Long memberId) {
        List<Attendance> attendances = attendanceRepository.findAllByMemberIdOrderBySessionStartsAtAsc(memberId);

        // SessionServiceImpl.detailSession()과 같은 방식: 전체 중 PRESENT 비율
        long presentCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        
        int rate = attendances.isEmpty() ? 0 : (int) Math.round((double) presentCount / attendances.size() * 100);

        return new MyAttendanceRateResDto(attendances.size(), (int) presentCount, rate);
    }

    @Override
    public MyStreakResDto getMyLongestStreak(Long memberId) {
        //회차 시작 시간으로 연속 PRESENT 구간 중 가장 긴 길이를 찾는다.
        List<Attendance> attendances = attendanceRepository.findAllByMemberIdOrderBySessionStartsAtAsc(memberId);

        int longest = 0;
        int current = 0;
        for (Attendance attendance : attendances) {
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 0;
            }
        }

        return new MyStreakResDto(longest);
    }

    @Override
    public List<MyAssignmentResDto> getMyDeadlineAssignments(Long memberId) {
        //이미 제출한 과제 ID를 먼저 확인
        List<Long> submittedAssignmentIds = submissionRepository.findSubmittedAssignmentIdsByMemberId(memberId);

        //참여 중인 스터디들의 마감 안 지난 과제 중, 제출한 것만 제외하고 반환
        return assignmentRepository.findUpcomingByMemberId(memberId, LocalDateTime.now())
        							.stream()
					                .filter(assignment -> !submittedAssignmentIds.contains(assignment.getId()))
					                .map(MemberConverter::toMyAssignmentResDto)
					                .toList();
    }

    @Override
    public List<MyApplicationResDto> getMyApplications(Long memberId, ApplicationStatus status) {

    	//status가 없으면(null) 전체 신청 내역, 있으면 해당 상태(대기/승인/거절)로 필터링된 신청 내역 조회
        List<Application> applications = (status == null) ? applicationRepository.findAllByApplicantId(memberId) : applicationRepository.findAllByApplicantIdAndStatus(memberId, status);

        return applications.stream().map(MemberConverter::toMyApplicationResDto).toList();
    }


    @Override
    public List<MyTodaySessionResDto> getMyTodaySessions(Long memberId) {

    	//오늘 하루에 해당하는 세션 조회
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Session> todaySessions = sessionRepository.findTodaySessionsByMemberId(memberId, start, end);

        return todaySessions.stream().map(MemberConverter::toMyTodaySessionResDto).toList();
    }
}