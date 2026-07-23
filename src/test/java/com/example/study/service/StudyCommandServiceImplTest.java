package com.example.study.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.repository.StudyRepository;

@ExtendWith(MockitoExtension.class)
class StudyCommandServiceImplTest {

    @InjectMocks
    private StudyCommandServiceImpl studyCommandService;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private Member testMember;
    private Study testStudy;

    private final Long memberId = 1L;
    private final Long studyId = 1L;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(memberId)
                .email("test@gmail.com")
                .build();

        testStudy = Study.builder()
                .id(studyId)
                .title("기존 스터디")
                .description("기존 설명")
                .capacity(5)
                .category(StudyCategory.ALGORITHM)
                .status(StudyStatus.RECRUITING)
                .isDeleted(false)
                .build();
    }

    @Nested
    @DisplayName("스터디 생성")
    class CreateStudy {

        @Test
        @DisplayName("Member가 존재하면 스터디를 생성하고 생성자를 LEADER로 저장")
        void createStudy_success() {
            // given
            CreateStudyReqDto reqDto = new CreateStudyReqDto("알고리즘 스터디", "알고리즘 문제 풀이 스터디", 6, null);
            Study savedStudy = Study.builder()
                    .id(studyId)
                    .title("알고리즘 스터디")
                    .description("알고리즘 문제 풀이 스터디")
                    .capacity(6)
                    .status(StudyStatus.RECRUITING)
                    .isDeleted(false)
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
            when(studyRepository.save(any(Study.class))).thenReturn(savedStudy);
            when(participantRepository.save(any(Participant.class)))
            						  .thenAnswer(invocation -> invocation.getArgument(0, Participant.class));
            // when
            StudyInfoResDto result = studyCommandService.createStudy(memberId, reqDto);

            // then
            assertThat(result).isNotNull();
            verify(memberRepository).findById(memberId);
            verify(studyRepository).save(any(Study.class));
            verify(participantRepository).save(any(Participant.class));
        }

        @Test
        @DisplayName("생성된 Participants는 요청한 Member와 생성된 스터디를 참조하고, 역할은 LEADER")
        void createStudy_createsLeaderParticipant() {
            // given
            CreateStudyReqDto reqDto = new CreateStudyReqDto("알고리즘 스터디", "알고리즘 문제 풀이 스터디", 5, null);

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
            when(studyRepository.save(any(Study.class))).thenReturn(testStudy);
            ArgumentCaptor<Participant> participantCaptor = ArgumentCaptor.forClass(Participant.class);

            // when
            studyCommandService.createStudy(memberId, reqDto);

            // then
            verify(participantRepository).save(participantCaptor.capture());
            Participant savedParticipant = participantCaptor.getValue();
            assertThat(savedParticipant.getMember()).isEqualTo(testMember);
            assertThat(savedParticipant.getStudy()).isEqualTo(testStudy);
            assertThat(savedParticipant.getRole()).isEqualTo(StudyRole.LEADER);
        }

        @Test
        @DisplayName("Member가 존재하지 않으면 예외 발생시키고 스터디 저장 X")
        void createStudy_memberNotFound() {
            // given
            CreateStudyReqDto reqDto = new CreateStudyReqDto("알고리즘 스터디", "알고리즘 문제 풀이 스터디", 5, null);
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.createStudy(memberId, reqDto)).isInstanceOf(GeneralException.class);
            verify(studyRepository, never()).save(any(Study.class));
            verify(participantRepository, never()).save(any(Participant.class));
        }
    }

    @Nested
    @DisplayName("스터디 수정")
    class UpdateStudy {

        @Test
        @DisplayName("스터디장은 스터디 정보 수정 가능")
        void updateStudy_success() {
            // given
            UpdateStudyReqDto reqDto = new UpdateStudyReqDto("수정된 스터디", "수정된 설명", 10, null);
            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(true);

            // when
            StudyInfoResDto result =studyCommandService.updateStudy(memberId, studyId, reqDto);

            // then
            assertThat(result).isNotNull();
            assertThat(testStudy.getTitle()).isEqualTo("수정된 스터디");
            assertThat(testStudy.getDescription()).isEqualTo("수정된 설명");
            assertThat(testStudy.getCapacity()).isEqualTo(10);
            verify(studyRepository).findById(studyId);
            verify(participantRepository).existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER);
        }

        @Test
        @DisplayName("스터디가 존재하지 않으면 수정 불가")
        void updateStudy_studyNotFound() {
            // given
            UpdateStudyReqDto reqDto = new UpdateStudyReqDto("수정된 스터디", null, 1, null);
            when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudy(memberId, studyId, reqDto)).isInstanceOf(GeneralException.class);

            verify(participantRepository, never()).existsByStudyIdAndMemberIdAndRole(any(), any(), any());
        }

        @Test
        @DisplayName("스터디장이 아니면 스터디 수정 불가")
        void updateStudy_forbidden() {
            // given
            UpdateStudyReqDto reqDto = new UpdateStudyReqDto("권한 없는 수정", null, 1, null);
            String originalTitle = testStudy.getTitle();

            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER )).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudy(memberId, studyId, reqDto)).isInstanceOf(GeneralException.class);
            assertThat(testStudy.getTitle()).isEqualTo(originalTitle);
        }

        @Test
        @DisplayName("PATCH 요청에서 null인 값은 기존 값 유지")
        void updateStudy_ignoresNullValues() {
            // given
            UpdateStudyReqDto reqDto = new UpdateStudyReqDto("제목수정", null, 1, null);
            String originalDescription = testStudy.getDescription();
            StudyCategory originalCategory = testStudy.getCategory();

            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(true);

            // when
            studyCommandService.updateStudy(memberId, studyId, reqDto);

            // then
            assertThat(testStudy.getTitle()).isEqualTo("제목수정");
            assertThat(testStudy.getCapacity()).isEqualTo(1);
            assertThat(testStudy.getDescription()).isEqualTo(originalDescription);
            assertThat(testStudy.getCategory()).isEqualTo(originalCategory);
        }
    }

    @Nested
    @DisplayName("스터디 상태 변경")
    class UpdateStudyStatus {

        @Test
        @DisplayName("스터디장은 스터디 상태 변경 가능")
        void updateStudyStatus_success() {
            // given
            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(true);

            // when
            StudyInfoResDto result =studyCommandService.updateStudyStatus(memberId, studyId, StudyStatus.CLOSED);

            // then
            assertThat(result).isNotNull();
            assertThat(testStudy.getStatus()).isEqualTo(StudyStatus.CLOSED);
        }

        @Test
        @DisplayName("스터디가 존재하지 않으면 상태 변경 불가능")
        void updateStudyStatus_studyNotFound() {
            // given
            when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudyStatus(memberId,studyId,StudyStatus.CLOSED))
            									 .isInstanceOf(GeneralException.class);
            verify(participantRepository, never()).existsByStudyIdAndMemberIdAndRole(any(), any(), any());
        }

        @Test
        @DisplayName("스터디장이 아니면 상태 변경 불가능")
        void updateStudyStatus_forbidden() {
            // given
            StudyStatus originalStatus =testStudy.getStatus();

            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudyStatus(memberId, studyId, StudyStatus.CLOSED))
            									 .isInstanceOf(GeneralException.class);
            assertThat(testStudy.getStatus()).isEqualTo(originalStatus);
        }
    }
    
    @Nested
    @DisplayName("스터디장 변경")
    class UpdateStudyLeader {
        private final Long newLeaderId = 2L;
        private Member newLeaderMember;
        private Participant currentLeaderParticipant;
        private Participant newLeaderParticipant;

        @BeforeEach
        void createParticipants() {
            newLeaderMember = Member.builder()
                    .id(newLeaderId)
                    .email("newleader@gmail.com")
                    .name("새로운 스터디장")
                    .build();

            currentLeaderParticipant = Participant.builder()
                    .study(testStudy)
                    .member(testMember)
                    .role(StudyRole.LEADER)
                    .build();

            newLeaderParticipant = Participant.builder()
                    .study(testStudy)
                    .member(newLeaderMember)
                    .role(StudyRole.MEMBER)
                    .build();
        }

        @Test
        @DisplayName("현재 스터디장은 같은 스터디의 participant에게 스터디장 위임 가능")
        void updateStudyLeader_success() {
            // given
            when(participantRepository.findByStudyIdAndMemberId(studyId, memberId)).thenReturn(Optional.of(currentLeaderParticipant));
            when(participantRepository.findByStudyIdAndMemberIdFetchJoinMember(studyId,newLeaderId)).thenReturn(Optional.of(newLeaderParticipant));

            // when
            UpdateStudyLeaderResDto result = studyCommandService.updateStudyLeader(memberId, studyId, newLeaderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.studyId()).isEqualTo(studyId);
            assertThat(result.newLeaderId()).isEqualTo(newLeaderId);
            assertThat(result.newLeaderName()).isEqualTo("새로운 스터디장");
            assertThat(currentLeaderParticipant.getRole()).isEqualTo(StudyRole.MEMBER);
            assertThat(newLeaderParticipant.getRole()).isEqualTo(StudyRole.LEADER);
        }

        @Test
        @DisplayName("현재 스터디장 Member가 해당 스터디의 스터디장이 아니면 위임 불가")
        void updateStudyLeader_currentMemberIsNotLeader() {
            // given
            Participant normalParticipant = Participant.builder()
                    .study(testStudy)
                    .member(testMember)
                    .role(StudyRole.MEMBER)
                    .build();

            when(participantRepository.findByStudyIdAndMemberId(studyId, memberId)).thenReturn(Optional.of(normalParticipant));

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudyLeader(memberId, studyId, newLeaderId)).isInstanceOf(GeneralException.class);
            assertThat(normalParticipant.getRole()).isEqualTo(StudyRole.MEMBER);
            verify(participantRepository, never()).findByStudyIdAndMemberIdFetchJoinMember(any(), any());
        }
        
        @Test
        @DisplayName("현재 스터디장 Member가 해당 스터디의 Member가 아니면 위임 불가")
        void updateStudyLeader_currentParticipantNotFound() {
            // given
            when(participantRepository.findByStudyIdAndMemberId(studyId, memberId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudyLeader(memberId, studyId, newLeaderId)).isInstanceOf(GeneralException.class);
            verify(participantRepository, never()).findByStudyIdAndMemberIdFetchJoinMember(any(), any());
        }

        @Test
        @DisplayName("새로운 스터디장 Member가 해당 스터디의 Member가 아니면 위임 불가")
        void updateStudyLeader_newLeaderNotFound() {
            // given
            when(participantRepository.findByStudyIdAndMemberId(studyId, memberId)).thenReturn(Optional.of(currentLeaderParticipant));
            when(participantRepository.findByStudyIdAndMemberIdFetchJoinMember(studyId, newLeaderId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.updateStudyLeader(memberId, studyId, newLeaderId)).isInstanceOf(GeneralException.class);
            assertThat(currentLeaderParticipant.getRole()).isEqualTo(StudyRole.LEADER);
            assertThat(newLeaderParticipant.getRole()).isEqualTo(StudyRole.MEMBER);
        }
    }

    @Nested
    @DisplayName("스터디 삭제")
    class SoftDeleteStudy {

        @Test
        @DisplayName("스터디장은 스터디 soft delete 가능")
        void softDeleteStudy_success() {
            // given
            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(true);

            // when
            studyCommandService.softDeleteStudy(memberId, studyId);

            // then
            assertThat(testStudy.isDeleted()).isTrue();
            verify(studyRepository).findById(studyId);
            verify(participantRepository).existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER);
        }

        @Test
        @DisplayName("스터디가 존재하지 않으면 삭제 불가능")
        void softDeleteStudy_studyNotFound() {
            // given
            when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> studyCommandService.softDeleteStudy(memberId, studyId)).isInstanceOf(GeneralException.class);
            verify(participantRepository, never()).existsByStudyIdAndMemberIdAndRole(any(), any(), any());
        }

        @Test
        @DisplayName("스터디장이 아니면 삭제 불가능")
        void softDeleteStudy_forbidden() {
            // given
            when(studyRepository.findById(studyId)).thenReturn(Optional.of(testStudy));
            when(participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> studyCommandService.softDeleteStudy(memberId, studyId)).isInstanceOf(GeneralException.class);
            assertThat(testStudy.isDeleted()).isFalse();
        }
    }
}