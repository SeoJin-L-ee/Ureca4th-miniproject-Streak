package com.example.participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;

public interface ParticipantRepository extends JpaRepository<Participant, Long>{
	// 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
}
