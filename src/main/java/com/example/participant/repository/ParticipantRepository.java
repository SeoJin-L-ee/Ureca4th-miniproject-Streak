package com.example.participant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;

public interface ParticipantRepository extends JpaRepository<Participant, Long>{
	// 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
	
	Optional<Participant> findByStudyIdAndMemberId(Long studyId, Long memberId);
	
	// 이후 연결된 Member 내부 필드를 얻어야 할 때 N+1 방지 목적
	@Query("SELECT p FROM Participant p JOIN FETCH p.member WHERE p.study.id = :studyId AND p.member.id = :memberId")
    Optional<Participant> findByStudyIdAndMemberIdFetchJoinMember(@Param("studyId") Long studyId, @Param("memberId") Long memberId);
}
