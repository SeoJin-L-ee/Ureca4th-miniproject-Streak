package com.example.participant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.participant.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	Optional<Participant> findByStudyIdAndMemberId(Long studyId, Long memberId);
}
