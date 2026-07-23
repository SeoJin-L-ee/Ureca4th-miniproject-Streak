package com.example.study.service;

import org.springframework.stereotype.Service;

import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.study.converter.StudyConverter;
import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.exception.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyCommandServiceImpl implements StudyCommandService {

	private final StudyRepository studyRepository;
	private final MemberRepository memberRepository;
	private final ParticipantRepository participantRepository;

	@Override
	@Transactional
	// 스터디 생성
	public StudyInfoResDto createStudy(Long memberId, CreateStudyReqDto reqDto) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
		
		// 스터디 생성 및 저장
		Study study = studyRepository.save(StudyConverter.toStudy(reqDto));
		
		// 참여자 생성 (스터디 생성 시점이므로 현재 유저가 LEADER)
		Participant participant = Participant.builder()
				.study(study)
				.member(member)
				.role(StudyRole.LEADER)
				.build();
		participantRepository.save(participant);
		return StudyConverter.toStudyInfoResDto(study);
	}

	@Override
	@Transactional
	// 스터디 수정
	public StudyInfoResDto updateStudy(Long memberId, Long studyId, UpdateStudyReqDto reqDto) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		// 수정하려는 Study 의 스터디장인 경우에만 수정 가능 (존재하지 않는 memberId인 경우에도 차단됨)
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		// null 이 아닌 값에 대해서만 업데이트 (PATCH)
		study.updateStudy(reqDto);
		return StudyConverter.toStudyInfoResDto(study);
	}

	@Override
	@Transactional
	// 스터디 상태 변경 (모집 중, 모집 완료, 종료됨)
	public StudyInfoResDto updateStudyStatus(Long memberId, Long studyId, StudyStatus status) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		// 수정하려는 Study 의 스터디장인 경우에만 수정 가능 (존재하지 않는 memberId인 경우에도 차단됨)
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		study.updateStatus(status);
		return StudyConverter.toStudyInfoResDto(study);
	}
	
	@Override
	@Transactional
	// 스터디장 변경 (위임)
	public UpdateStudyLeaderResDto updateStudyLeader(Long memberId, Long studyId, Long newLeaderId) {
		// studyId 에 해당하는 Study 가 존재하지 않을 경우에 대한 에러 코드 분리 (미니 프로젝트이므로 성능 최적화보다 명확한 에러처리를 우선시)
		if (!studyRepository.existsById(studyId)) throw new GeneralException(StudyErrorCode.STUDY_NOT_FOUND);
		// 해당 Study 의 스터디장인 경우에만 위임 가능
		Participant currentLeader = participantRepository.findByStudyIdAndMemberId(studyId, memberId)
	            .filter(p -> p.getRole() == StudyRole.LEADER)
	            .orElseThrow(() -> new GeneralException(CommonErrorCode.FORBIDDEN));
		// newLeaderId 가 현재 Study 에 속하는 memberId 여야 함 (DTO 변환을 위해서 Member Fetch join)
		Participant newLeader = participantRepository.findByStudyIdAndMemberIdFetchJoinMember(studyId, newLeaderId)
				.orElseThrow(() -> new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND));
		
		// 원래 스터디장(현재 유저)을 MEMBER 로 변경한 후, 새로운 newLeader를 LEADER 로 변경
		currentLeader.updateRole(StudyRole.MEMBER);
	    newLeader.updateRole(StudyRole.LEADER);
		return StudyConverter.toUpdateStudyLeaderResDto(newLeader);
	}

	@Override
	@Transactional
	// 스터디 soft delete (DELETED)
	public void softDeleteStudy(Long memberId, Long studyId) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		// 수정하려는 Study 의 스터디장인 경우에만 수정 가능 (존재하지 않는 memberId인 경우에도 차단됨)
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		study.updateIsDeleted(true);
	}
	
}
