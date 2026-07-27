import { api } from "./client";
import type { AttendanceListResDto, AttendanceSessionResDto, AttendanceStatus } from "./types";

export function getMemberAttendances(studyId: number) {
  return api.get<AttendanceListResDto>(`/api/studies/${studyId}/attendances`);
}

export function getSessionAttendances(studyId: number, sessionId: number) {
  return api.get<AttendanceSessionResDto>(`/api/studies/${studyId}/sessions/${sessionId}/attendances`);
}

export function updateSessionAttendances(
  studyId: number,
  sessionId: number,
  attendances: { memberId: number; status: AttendanceStatus }[]
) {
  return api.patch<void>(`/api/studies/${studyId}/sessions/${sessionId}/attendances`, { attendances });
}
