import type { ApplicationStatus, AttendanceStatus, StudyCategory, StudyRole, StudyStatus } from "../api/types";

export const categoryLabel: Record<StudyCategory, string> = {
  ALGORITHM: "알고리즘",
  ENGLISH: "어학",
  CERTIFICATE: "자격증",
  ETC: "기타",
};

export const studyStatusLabel: Record<StudyStatus, string> = {
  RECRUITING: "모집중",
  CLOSED: "모집완료",
  ENDED: "종료",
};

export const studyStatusTone: Record<StudyStatus, "green" | "amber" | "gray"> = {
  RECRUITING: "amber",
  CLOSED: "green",
  ENDED: "gray",
};

export const roleLabel: Record<StudyRole, string> = {
  LEADER: "스터디장",
  MEMBER: "참여자",
};

export const attendanceStatusLabel: Record<AttendanceStatus, string> = {
  UNMARKED: "예정",
  PRESENT: "출석",
  ABSENT: "결석",
};

export const attendanceStatusTone: Record<AttendanceStatus, "green" | "red" | "gray"> = {
  UNMARKED: "gray",
  PRESENT: "green",
  ABSENT: "red",
};

export const applicationStatusLabel: Record<ApplicationStatus, string> = {
  PENDING: "대기",
  APPROVED: "승인",
  REJECTED: "거절",
};

export const applicationStatusTone: Record<ApplicationStatus, "amber" | "green" | "red"> = {
  PENDING: "amber",
  APPROVED: "green",
  REJECTED: "red",
};
