import { api } from "./client";
import type { SessionInfoResDto, SessionResDto } from "./types";

export function createSession(studyId: number, payload: { sessionNumber: number; title: string; content: string; startsAt: string }) {
  return api.post<SessionResDto>(`/api/studies/${studyId}/sessions`, payload);
}

export function updateSession(
  studyId: number,
  sessionId: number,
  payload: Partial<{ sessionNumber: number; title: string; content: string; startsAt: string }>
) {
  return api.patch<SessionResDto>(`/api/studies/${studyId}/sessions/${sessionId}`, payload);
}

export function deleteSession(studyId: number, sessionId: number) {
  return api.delete<void>(`/api/studies/${studyId}/sessions/${sessionId}`);
}

export function getSessionDetail(studyId: number, sessionId: number) {
  return api.get<SessionInfoResDto>(`/api/studies/${studyId}/sessions/${sessionId}`);
}
