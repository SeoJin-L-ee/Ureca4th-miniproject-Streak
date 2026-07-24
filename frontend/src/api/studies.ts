import { api } from "./client";
import type {
  StudyCategory,
  StudyDashboardResDto,
  StudyInfoResDto,
  StudyStatus,
  StudySummaryListResDto,
  UpdateStudyLeaderResDto,
} from "./types";

export function getMyStudyList(page = 0, size = 10) {
  return api.get<StudySummaryListResDto>(`/api/studies?page=${page}&size=${size}`);
}

export function getStudyDashboard(studyId: number, page = 0, size = 20) {
  return api.get<StudyDashboardResDto>(`/api/studies/${studyId}/dashboard?page=${page}&size=${size}`);
}

export function createStudy(payload: { title: string; description: string; capacity: number; category: StudyCategory }) {
  return api.post<StudyInfoResDto>("/api/studies", payload);
}

export function updateStudy(
  studyId: number,
  payload: { title: string; description: string; capacity: number; category: StudyCategory }
) {
  return api.patch<StudyInfoResDto>(`/api/studies/${studyId}`, payload);
}

export function updateStudyStatus(studyId: number, status: StudyStatus) {
  return api.patch<StudyInfoResDto>(`/api/studies/${studyId}/status?status=${status}`);
}

export function updateStudyLeader(studyId: number, newLeaderId: number) {
  return api.patch<UpdateStudyLeaderResDto>(`/api/studies/${studyId}/leader?newLeaderId=${newLeaderId}`);
}

export function deleteStudy(studyId: number) {
  return api.delete<void>(`/api/studies/${studyId}`);
}
