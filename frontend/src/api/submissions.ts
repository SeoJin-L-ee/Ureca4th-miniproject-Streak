import { api } from "./client";
import type { SubmissionListResDto, SubmissionSummaryResDto } from "./types";

export function createSubmission(studyId: number, sessionId: number, assignmentId: number, content: string) {
  return api.post<SubmissionSummaryResDto>(
    `/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}/submissions`,
    { content }
  );
}

export function updateSubmission(
  studyId: number,
  sessionId: number,
  assignmentId: number,
  submissionId: number,
  content: string
) {
  return api.patch<SubmissionSummaryResDto>(
    `/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}/submissions/${submissionId}`,
    { content }
  );
}

export function deleteSubmission(studyId: number, sessionId: number, assignmentId: number, submissionId: number) {
  return api.delete<void>(
    `/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}/submissions/${submissionId}`
  );
}

export function listSubmissions(studyId: number, sessionId: number, assignmentId: number) {
  return api.get<SubmissionListResDto>(`/api/studies/${studyId}/sessions/${sessionId}/assignments/${assignmentId}/submissions`);
}
