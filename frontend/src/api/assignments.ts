import { api } from "./client";
import type { AssignmentInfoResDto, AssignmentListResDto } from "./types";

export function createAssignment(
  studyId: number,
  sessionId: number,
  payload: { title: string; description: string; dueAt: string }
) {
  return api.post<AssignmentInfoResDto>(`/api/studies/${studyId}/sessions/${sessionId}/assignments`, payload);
}

export function updateAssignment(
  studyId: number,
  sessionId: number,
  assignmentId: number,
  payload: Partial<{ title: string; description: string; dueAt: string }>
) {
  return api.patch<AssignmentInfoResDto>(`/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}`, payload);
}

export function deleteAssignment(studyId: number, sessionId: number, assignmentId: number) {
  return api.delete<void>(`/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}`);
}

export function getAssignmentDetail(studyId: number, sessionId: number, assignmentId: number) {
  return api.get<AssignmentInfoResDto>(`/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}`);
}

export function listAssignments(studyId: number, sessionId: number) {
  return api.get<AssignmentListResDto>(`/api/studies/${studyId}/sessions/${sessionId}/assignments`);
}
