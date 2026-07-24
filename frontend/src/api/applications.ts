import { api } from "./client";
import type { ApplicationResDto, ApplicationStatus } from "./types";

export function applyToStudy(studyId: number, content: string) {
  return api.post<ApplicationResDto>(`/api/studies/${studyId}/applications`, { content });
}

export function updateApplicationStatus(applicationId: number, status: ApplicationStatus) {
  return api.patch<ApplicationResDto>(`/api/applications/${applicationId}/status`, { status });
}

export function deleteMyApplication(applicationId: number) {
  return api.delete<void>(`/api/applications/${applicationId}`);
}
