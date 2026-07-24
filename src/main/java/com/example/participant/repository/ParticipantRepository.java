package com.example.participant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.study.dto.response.StudyLeaderDto;
import com.example.study.dto.response.StudyParticipantCountDto;
import com.example.study.entity.enums.StudyStatus;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
  
	// 스터디장 여부 확인 시 사용
	boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole studyRole);
	
	// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
	boolean existsByStudyIdAndMemberId(Long studyId, Long memberId);
	
	long countByStudyId(Long studyId);
	
	// 스터디에 참여한 모든 member 조회 
	List<Participant> findAllByStudyId(Long studyId);
	
	// 스터디에 참여한 모든 Member를 조회 
	@Query("SELECT p.member FROM Participant p WHERE p.study.id = :studyId")
	List<Member> findMembersByStudyId(@Param("studyId") Long studyId);
	
	// 스터디에 참여한 모든 Participant 조회 시 Member 정보도 함께 Fetch Join 
	@Query("SELECT p FROM Participant p JOIN FETCH p.member WHERE p.study.id = :studyId")
	List<Participant> findAllByStudyIdFetchJoinMember(@Param("studyId") Long studyId);
	
	// 스터디 ID와 회원 ID로 해당 스터디의 참여자 정보 조회
	@Query("SELECT p.member FROM Participant p WHERE p.study.id = :studyId AND p.member.id = :memberId")
	Optional<Member> findMemberByStudyIdAndMemberId(@Param("studyId") Long studyId, @Param("memberId") Long memberId);

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
	
	//마이페이지 - 참여 중인 스터디 목록 조회용. Study를 함께 fetch해서 N+1 방지
	@Query("""
			SELECT p FROM Participant p
			JOIN FETCH p.study
			WHERE p.member.id = :memberId
			""")
	List<Participant> findAllByMemberIdFetchStudy(@Param("memberId") Long memberId);
	
	// 여러 스터디의 참여자 수를 한 번에 계산
	@Query("""
			SELECT new com.example.study.dto.response.StudyParticipantCountDto(
				p.study.id,
				COUNT(p))
			FROM Participant p
			WHERE p.study.id IN :studyIds
			GROUP BY p.study.id
			""")
	List<StudyParticipantCountDto> countParticipantsByStudyIds(@Param("studyIds") List<Long> studyIds);
	
	// 특정 스터디의 LEADER 권한을 가진 Participant(Member 포함) 단건 조회
	@Query("""
			SELECT p
			FROM Participant p
				JOIN FETCH p.member
			WHERE p.study.id = :studyId
				AND p.role = :role
			""")
	Optional<Participant> findLeaderByStudyId(
			@Param("studyId") Long studyId,
    		@Param("role") StudyRole leaderRole
    );
	
	// 여러 스터디의 스터디장 정보(스터디 id + 스터디장 이름)를 한번에 조회
	@Query("""
			SELECT new com.example.study.dto.response.StudyLeaderDto(
				p.study.id, p.member.name)
			FROM Participant p
			WHERE p.study.id IN :studyIds
				AND p.role = :role
			""")
    List<StudyLeaderDto> findLeadersByStudyIds(
    		@Param("studyIds") List<Long> studyIds,
    		@Param("role") StudyRole leaderRole
    );
	
}
