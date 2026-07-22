package com.example.participant.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.study.entity.enums.StudyStatus;

public interface ParticipantRepository extends JpaRepository<Participant, Long>{
	// 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
  
  // 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);
	
	Optional<Participant> findByStudyIdAndMemberId(Long studyId, Long memberId);
	
	// 이후 연결된 Member 내부 필드를 얻어야 할 때 N+1 방지 목적
	@Query("SELECT p FROM Participant p JOIN FETCH p.member WHERE p.study.id = :studyId AND p.member.id = :memberId")
    Optional<Participant> findByStudyIdAndMemberIdFetchJoinMember(@Param("studyId") Long studyId, @Param("memberId") Long memberId);
	
	// 사용자가 참여 중인 스터디 목록 조회 시 사용 (isLeader 필드를 얻기 위해 Study 가 아닌 Participants 조회)
	// 	-> Study 도 함께 가져오기 위해 Join fetch + Pageable 을 함께 사용하면,
	//	   hibernate 는 오프셋으로 끊어서 가져오지 못하고, 모든 데이터를 메모리로 가져온 후 페이징을 시도한다고 함.
	// 	-> @EntityGraph 를 사용하면 join fetch 없이도 지연 로딩 객체를 같이 가져올 수 있음
	@EntityGraph(attributePaths = {"study"})
	@Query("""
			SELECT p FROM Participant p 
			WHERE p.member.id = :memberId
				AND p.study.status != :status
			    AND p.study.isDeleted = false
			""")
	Page<Participant> findParticipantsByMemberId(
	        @Param("memberId") Long memberId, 
	        @Param("status") StudyStatus status, 
	        Pageable pageable
	);
}
