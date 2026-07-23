package com.example.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.request.BatchSaveAttendanceReqDto;
import com.example.attendance.dto.request.UpdateAttendanceReqDto;
import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceMemberResDto;
import com.example.attendance.dto.response.AttendanceParticipantResDto;
import com.example.attendance.dto.response.AttendanceSessionResDto;
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
    
    
    @Nested
    @Order(4)
    @DisplayName("회차별 참여자 출석 목록 조회 테스트")
    class GetSessionAttendancesTest {

        @Test
        @DisplayName("성공: 스터디장은 회차별 참여자 출석 목록을 조회할 수 있다.")
        void getSessionAttendances_Success() {
            // given
        	// member1을 LEADER로 설정 
        	participantRepository.save(
        		Participant.builder()
        				.study(study)
        				.member(member1)
                        .role(StudyRole.LEADER)
                        .build()
            );
        	
        	// member2를 일반 MEMBER로 등록
            participantRepository.save(
            	Participant.builder()
            			.study(study)
                        .member(member2)
                        .role(StudyRole.MEMBER)
                        .build()
            );
            
            attendanceRepository.save(
            	Attendance.builder()
            			.session(session1)
            			.member(member2)
            			.status(AttendanceStatus.PRESENT)
            			.build()
            );

            // when
            AttendanceSessionResDto result = attendanceService.getSessionAttendances(
                    study.getId(), 
                    session1.getId(), 
                    member1.getId()
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo(session1.getId());
            
            // 참여자 2명(member1, member2) 확인
            List<AttendanceParticipantResDto> participants = result.participants();
            assertThat(participants).hasSize(2);

            // 개별 출석 상태 매핑 검증
            assertThat(participants)
                    .extracting("memberId", "status")
                    .containsExactlyInAnyOrder(
                            tuple(member1.getId(), null),                      // 출석 정보 없음 (null)
                            tuple(member2.getId(), AttendanceStatus.PRESENT)   // 출석 (PRESENT)
                    );
        }

        @Test
        @DisplayName("실패: 스터디장이 아닌 스터디원이 조회 시 예외가 발생한다.")
        void getSessionAttendances_NotLeader() {
        	
        	// given - member2는 LEADER가 아닌 MEMBER로 등록
        	participantRepository.save(
                    Participant.builder()
                            .study(study)
                            .member(member2)
                            .role(StudyRole.MEMBER)
                            .build()
            );
        	
            // when & then (MEMBER인 member2가 요청 시 예외 발생)
            assertThatThrownBy(() -> attendanceService.getSessionAttendances(
                    study.getId(), 
                    session1.getId(), 
                    member2.getId()
            ))
            .isInstanceOf(GeneralException.class);
        }
    }
    
    
    
    @Nested
    @Order(5)
    @DisplayName("참여자 출석 사항 저장(수정) - 스터디장 전용 테스트")
    class UpdateSessionAttendancesTest {

        @Test
        @DisplayName("성공: 스터디장은 회차별 출석 정보를 새로 저장하거나 수정할 수 있다.")
        void updateSessionAttendances_Success() {
        	
            // given
            // member1을 LEADER, member2를 MEMBER로 등록
            participantRepository.save(
                    Participant.builder()
                            .study(study)
                            .member(member1)
                            .role(StudyRole.LEADER)
                            .build()
            );
            
            participantRepository.save(
                    Participant.builder()
                            .study(study)
                            .member(member2)
                            .role(StudyRole.MEMBER)
                            .build()
            );

            // 기존 출석 데이터 세팅 (member1: 기존 PRESENT 출석 기록 존재)
            attendanceRepository.save(
                    Attendance.builder()
                            .session(session1)
                            .member(member1)
                            .status(AttendanceStatus.PRESENT)
                            .build()
            );

            // 요청 DTO 준비 (member1: PRESENT -> ABSENT로 수정, member2: 신규 PRESENT 저장)
            UpdateAttendanceReqDto dto1 = new UpdateAttendanceReqDto(member1.getId(), AttendanceStatus.ABSENT);
            UpdateAttendanceReqDto dto2 = new UpdateAttendanceReqDto(member2.getId(), AttendanceStatus.PRESENT);
            BatchSaveAttendanceReqDto reqDto = new BatchSaveAttendanceReqDto(List.of(dto1, dto2));

            // when
            attendanceService.updateSessionAttendances(study.getId(), session1.getId(), member1.getId(), reqDto);

            // then
            List<Attendance> savedAttendances = attendanceRepository.findAllBySessionId(session1.getId());
            assertThat(savedAttendances).hasSize(2);

            // member1의 상태가 ABSENT로 변경되었는지 확인
            Attendance updatedMember1 = savedAttendances.stream()
                    .filter(a -> a.getMember().getId().equals(member1.getId()))
                    .findFirst()
                    .orElseThrow();
            
            assertThat(updatedMember1.getStatus()).isEqualTo(AttendanceStatus.ABSENT);

            // member2의 출석 정보가 신규 생성되었는지 확인
            Attendance newMember2 = savedAttendances.stream()
                    .filter(a -> a.getMember().getId().equals(member2.getId()))
                    .findFirst()
                    .orElseThrow();
            
            assertThat(newMember2.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        }

        @Test
        @DisplayName("실패: 스터디장이 아닌 일반 멤버가 출석 저장을 시도하면 예외가 발생한다.")
        void updateSessionAttendances_NotLeader() {
        	
            // given
            participantRepository.save(
                    Participant.builder()
                            .study(study)
                            .member(member2)
                            .role(StudyRole.MEMBER)
                            .build()
            );

            UpdateAttendanceReqDto dto = new UpdateAttendanceReqDto(member2.getId(), AttendanceStatus.PRESENT);
            BatchSaveAttendanceReqDto reqDto = new BatchSaveAttendanceReqDto(List.of(dto));

            // when & then (MEMBER인 member2가 요청 시 예외 발생)
            assertThatThrownBy(() -> attendanceService.updateSessionAttendances(
                    study.getId(), session1.getId(), member2.getId(), reqDto
            ))
            .isInstanceOf(GeneralException.class);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 세션 ID로 출석 저장 시 예외가 발생한다.")
        void updateSessionAttendances_SessionNotFound() {
        	
            // given
            participantRepository.save(
                    Participant.builder()
                            .study(study)
                            .member(member1)
                            .role(StudyRole.LEADER)
                            .build()
            );

            UpdateAttendanceReqDto dto = new UpdateAttendanceReqDto(member1.getId(), AttendanceStatus.PRESENT);
            BatchSaveAttendanceReqDto reqDto = new BatchSaveAttendanceReqDto(List.of(dto));
            long invalidSessionId = 999L;

            // when & then
            assertThatThrownBy(() -> attendanceService.updateSessionAttendances(
                    study.getId(), invalidSessionId, member1.getId(), reqDto
            ))
            .isInstanceOf(GeneralException.class);
        }
    }


    @Nested
    @Order(6)
    @DisplayName("멤버의 최장 연속 출석일(Streak) 계산 테스트")
    class GetMyLongestStreakTest {

        @Test
        @DisplayName("하루 차이로 연속 출석한 구간만 스트릭으로 인정한다.")
        void getMyLongestStreak_ConsecutiveDaysOnly() {

            // given
            LocalDateTime day1 = LocalDateTime.now();

            Session s1 = sessionRepository.save(Session.builder().study(study).sessionNumber(10).title("streak1").content("내용").startsAt(day1).build());
            Session s2 = sessionRepository.save(Session.builder().study(study).sessionNumber(11).title("streak2").content("내용").startsAt(day1.plusDays(1)).build());   // 하루 뒤 -> 연속
            Session s3 = sessionRepository.save(Session.builder().study(study).sessionNumber(12).title("streak3").content("내용").startsAt(day1.plusDays(3)).build());   // 이틀 이상 벌어짐 -> 끊김
            Session s4 = sessionRepository.save(Session.builder().study(study).sessionNumber(13).title("streak4").content("내용").startsAt(day1.plusDays(4)).build());   // 하루 뒤 -> 새로 연속 시작
            Session s5 = sessionRepository.save(Session.builder().study(study).sessionNumber(14).title("streak5").content("내용").startsAt(day1.plusDays(5)).build());   // 하루 뒤 -> 연속

            attendanceRepository.save(Attendance.builder().session(s1).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(s2).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(s3).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(s4).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(s5).member(member1).status(AttendanceStatus.PRESENT).build());

            // when
            int longest = attendanceService.getMyLongestStreak(member1.getId());

            // then
            // day1,day2 = 2일 연속 / 이후 2일 차이라 끊김 / 마지막 3일 연속 -> 최장 3
            assertThat(longest).isEqualTo(3);
        }

        @Test
        @DisplayName("다른 스터디의 출석은 서로 섞이지 않고 스터디별로 따로 계산한다.")
        void getMyLongestStreak_SeparatesByStudy() {

            // given
            Study studyB = studyRepository.save(
                    Study.builder().title("스터디B").description("설명").capacity(10).category(StudyCategory.ALGORITHM).status(StudyStatus.RECRUITING).build()
            );

            LocalDateTime day1 = LocalDateTime.now();

            // 스터디A(기존 study): day1, day2 출석 -> 연속 2일
            Session aSession1 = sessionRepository.save(Session.builder().study(study).sessionNumber(20).title("A1").content("내용").startsAt(day1).build());
            Session aSession2 = sessionRepository.save(Session.builder().study(study).sessionNumber(21).title("A2").content("내용").startsAt(day1.plusDays(1)).build());

            // 스터디B: day2, day3 출석 -> 연속 2일 (합쳐지면 3으로 잘못 계산될 수 있음)
            Session bSession1 = sessionRepository.save(Session.builder().study(studyB).sessionNumber(1).title("B1").content("내용").startsAt(day1.plusDays(1)).build());
            Session bSession2 = sessionRepository.save(Session.builder().study(studyB).sessionNumber(2).title("B2").content("내용").startsAt(day1.plusDays(2)).build());

            attendanceRepository.save(Attendance.builder().session(aSession1).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(aSession2).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(bSession1).member(member1).status(AttendanceStatus.PRESENT).build());
            attendanceRepository.save(Attendance.builder().session(bSession2).member(member1).status(AttendanceStatus.PRESENT).build());

            // when
            int longest = attendanceService.getMyLongestStreak(member1.getId());

            // then
            // 스터디별로 계산하면 A=2, B=2 이므로 최댓값은 2 (전체를 합쳤다면 3이 나왔을 것)
            assertThat(longest).isEqualTo(2);
        }
    }
}