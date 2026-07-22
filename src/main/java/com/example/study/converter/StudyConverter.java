package com.example.study.converter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.session.entity.Session;
import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.dto.response.StudySummaryListResDto;
import com.example.study.dto.response.StudySummaryResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyStatus;

public class StudyConverter {
	
	// CreateStudyReqDto -> Study
	public static Study toStudy(CreateStudyReqDto reqDto) {
		return Study.builder()
				.title(reqDto.title())
				.description(reqDto.description())
				.capacity(reqDto.capacity())
				.category(reqDto.category())
				.status(StudyStatus.RECRUITING)
				.isDeleted(false)
				.build();
	}
	
	// Study -> StudyInfoResDto
	public static StudyInfoResDto toStudyInfoResDto(Study study) {
		return new StudyInfoResDto(
				study.getTitle(),
				study.getDescription(),
				study.getCapacity(),
				study.getCategory(),
				study.getStatus()
		);
	}
	
	// Participant -> UpdateStudyLeaderResDto
	public static UpdateStudyLeaderResDto toUpdateStudyLeaderResDto(Participant participant) {
		return new UpdateStudyLeaderResDto(
				participant.getStudy().getId(),
				participant.getMember().getId(),
				participant.getMember().getName()
		);
	}
    
	// List<Participant> + 기타 정보 Map 들 -> List<StudySummaryResDto>
	public static List<StudySummaryResDto> toStudySummaryResDtoList(
			List<Participant> participants,
			Map<Long, Session> nextSessionByStudyId,
			Map<Long, Double> attendanceRateByStudyId
	) {
		return participants.stream()
				.map(p -> toStudySummaryResDto(
						p,
						nextSessionByStudyId.get(p.getStudy().getId()),
						attendanceRateByStudyId.get(p.getStudy().getId())
				)).toList();
	}
	
	// 데이터를 묶어서 StudySummaryResDto 반환 (toStudySummaryResDtoList 내부에서 호출됨)
	public static StudySummaryResDto toStudySummaryResDto(
			Participant participant,
			Session nextSession,
			Double attendanceRate
	) {
		Study study = participant.getStudy();
		// 다음 회차 정보를 정해진 문자열로 변환, ex) 다음 회차 - [6회차] 7/22 (Wed) 17:00
		String nextSessionInfo;
		if (nextSession == null) {
			nextSessionInfo = "예정된 다음 회차가 없습니다";
		} else {
			nextSessionInfo = "다음 회차 - [%d회차] %s".formatted(
					nextSession.getSessionNumber(),
					nextSession.getStartsAt().format(DateTimeFormatter.ofPattern("M/d (E) HH:mm"))
			);
		}
		return new StudySummaryResDto(
				study.getId(),
				study.getTitle(),
				study.getCategory(),
				nextSessionInfo,
				participant.getRole() == StudyRole.LEADER,
				// 지난 회차가 없는 경우 get 했을 때 null 나올 것 대비
				attendanceRate != null ? attendanceRate : 0.0
		);
	}
	
	// Page<Participant> + List<StudySummaryResDto> -> StudySummaryListResDto
	public static StudySummaryListResDto toStudySummaryListResDto(
			Page<Participant> participantPage,
			List<StudySummaryResDto> studySummaryResDtos
	) {
		return new StudySummaryListResDto(
				participantPage.getNumber(),
				participantPage.getSize(),
				participantPage.getTotalPages(),
				participantPage.getTotalElements(),
				participantPage.hasNext(),
				studySummaryResDtos
		);
	}
	
}
