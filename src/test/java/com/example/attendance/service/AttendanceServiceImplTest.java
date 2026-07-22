package com.example.attendance.service;

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
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
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
import com.example.session.entity.Session;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.repository.StudyRepository;

@SpringBootTest 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class AttendanceServiceImplTest {

    @Autowired
    private AttendanceServiceImpl attendanceService;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudyRepository studyRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    private Member member1;
    private Member member2;
    private Study study;
    private Session session1;
    private Session session2;
    
    @BeforeEach
    void setUp() {
    	
        // 공통 Member 2명 생성 및 저장
        member1 = memberRepository.save(
                Member.builder()
                        .email("test1@example.com")
                        .name("테스터1")
                        .password("password123!")
                        .phone("010-1234-5678")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        member2 = memberRepository.save(
                Member.builder()
                        .email("test2@example.com")
                        .name("테스터2")
                        .password("password123!")
                        .phone("010-8765-4321")
                        .status(MemberStatus.ACTIVE)
                        .build()
        );

        // 공통 Study 생성 및 저장
        study = studyRepository.save(
                Study.builder()
                        .title("스터디 제목")
                        .description("설명")
                        .capacity(10)
                        .category(StudyCategory.ALGORITHM)
                        .status(StudyStatus.RECRUITING)
                        .build()
        );

        // 공통 Session 생성 및 저장 
        session1 = sessionRepository.save(
                Session.builder()
                        .study(study)
                        .sessionNumber(1)
                        .title("1회차 세션")
                        .content("세션 내용")
                        .startsAt(LocalDateTime.now())
                        .build()
        );
        
        session2 = sessionRepository.save(
                Session.builder()
                        .study(study)
                        .sessionNumber(2)
                        .title("2회차 세션")
                        .content("세션 내용")
                        .startsAt(LocalDateTime.now().plusDays(7))
                        .build()
        );
    }
    
    @Test
    @Order(1)
    @DisplayName("스터디 참여자는 해당 스터디의 참여자별 출석 현황을 성공적으로 조회한다.")
    void getMemberAttendances_Success() {
    	
        // given
        // Participant 생성 및 저장
        participantRepository.save(
        		Participant.builder()
        			.study(study)
        			.member(member1)
        			.role(StudyRole.MEMBER)
        			.build()
        );
        
        participantRepository.save(
        		Participant.builder()
	        		.study(study)
	        		.member(member2)
	        		.role(StudyRole.MEMBER)
	        		.build()
        );

        // 출석 데이터 생성 및 저장 (회원1: 출석 2, 결석 0 / 회원2: 출석 1, 결석 1)
        attendanceRepository.save(Attendance.builder().session(session1).member(member1).status(AttendanceStatus.PRESENT).build());
        attendanceRepository.save(Attendance.builder().session(session2).member(member1).status(AttendanceStatus.PRESENT).build());

        attendanceRepository.save(Attendance.builder().session(session1).member(member2).status(AttendanceStatus.PRESENT).build());
        attendanceRepository.save(Attendance.builder().session(session2).member(member2).status(AttendanceStatus.ABSENT).build());

        // when
        AttendanceListResDto result = attendanceService.getMemberAttendances(study.getId(), member1.getId());

        
        // then
        assertThat(result).isNotNull();
        assertThat(result.studyId()).isEqualTo(study.getId());
        assertThat(result.members()).hasSize(2);

        
        // 회원1 검증 (출석률 100%)
        AttendanceMemberResDto member1Dto = result.members().stream()
                .filter(m -> m.memberId().equals(member1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(member1Dto.attendedCount()).isEqualTo(2);
        assertThat(member1Dto.absentCount()).isEqualTo(0);
        assertThat(member1Dto.attendanceRate()).isEqualTo(100.0);

        
        // 회원2 검증 (출석률 50%)
        AttendanceMemberResDto member2Dto = result.members().stream()
                .filter(m -> m.memberId().equals(member2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(member2Dto.attendedCount()).isEqualTo(1);
        assertThat(member2Dto.absentCount()).isEqualTo(1);
        assertThat(member2Dto.attendanceRate()).isEqualTo(50.0);

        // 스터디 전체 평균 출석률 검증 ((100 + 50) / 2 = 75.0%)
        assertThat(result.averageAttendanceRate()).isEqualTo(75.0);
    }


    @Test
    @Order(2)
    @DisplayName("스터디에 참여하지 않은 멤버가 출석 현황을 조회하면 FORBIDDEN 예외가 발생한다.")
    void getMemberAttendances_ForbiddenException() {
    	
        // given - member1을 partitipant에 넣지 않음 

        // when & then
        assertThatThrownBy(() -> attendanceService.getMemberAttendances(study.getId(), member1.getId()))
                .isInstanceOf(GeneralException.class);
    }


    @Test
    @Order(3)
    @DisplayName("출석/결석 기록이 하나도 없는 경우 출석률은 0.0으로 계산된다.")
    void getMemberAttendances_ZeroAttendanceRate() {
    	
        // given
        participantRepository.save(
        		Participant.builder()
        			.study(study)
        			.member(member1)
        			.role(StudyRole.MEMBER)
        			.build()
        );

        // when
        AttendanceListResDto result = attendanceService.getMemberAttendances(study.getId(), member1.getId());

        // then
        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).attendanceRate()).isEqualTo(0.0);
        assertThat(result.averageAttendanceRate()).isEqualTo(0.0);
    }
}