package com.example.participant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
  
	// 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
	
	// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
	boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);
	
	// 스터디에 참여한 모든 member 조회 
	List<Participant> findAllByStudyId(Long studyId);
	
	// 스터디에 참여한 모든 Member를 조회 
	@Query("SELECT p.member FROM Participant p WHERE p.study.id = :studyId")
	List<Member> findMembersByStudyId(@Param("studyId") Long studyId);
	
	// 스터디 ID와 회원 ID로 해당 스터디의 참여자 정보 조회
	@Query("SELECT p.member FROM Participant p WHERE p.study.id = :studyId AND p.member.id = :memberId")
	Optional<Member> findMemberByStudyIdAndMemberId(@Param("studyId") Long studyId, @Param("memberId") Long memberId);

	Optional<Participant> findByStudyIdAndMemberId(Long studyId, Long memberId);
	
	// 이후 연결된 Member 내부 필드를 얻어야 할 때 N+1 방지 목적
	@Query("SELECT p FROM Participant p JOIN FETCH p.member WHERE p.study.id = :studyId AND p.member.id = :memberId")
    Optional<Participant> findByStudyIdAndMemberIdFetchJoinMember(@Param("studyId") Long studyId, @Param("memberId") Long memberId);
}