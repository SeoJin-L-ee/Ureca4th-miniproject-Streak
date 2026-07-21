package com.example.participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
	
	// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
	boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);
}
