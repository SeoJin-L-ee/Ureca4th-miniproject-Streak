import { api } from "./client";
import type {
  StudyApplyDetailResDto,
  StudyApplySummaryListResDto,
  StudyCategory,
  StudyDashboardResDto,
  StudyInfoResDto,
  StudyStatus,
  StudySummaryListResDto,
  UpdateStudyLeaderResDto,
} from "./types";

export function getMyStudyList(page = 0, size = 10) {
  return api.get<StudySummaryListResDto>(`/api/studies/me?page=${page}&size=${size}`);
}

// 스터디 탐색 목록 조회 (전체, 카테고리별, 제목 검색)
export function getStudiesForApply(params: { category?: StudyCategory; title?: string; page?: number; size?: number } = {}) {
  const qs = new URLSearchParams();
  if (params.category) qs.set("category", params.category);
  if (params.title) qs.set("title", params.title);
  qs.set("page", String(params.page ?? 0));
  qs.set("size", String(params.size ?? 20));
  return api.get<StudyApplySummaryListResDto>(`/api/studies?${qs.toString()}`);
}

// 스터디 상세 조회 (스터디 찾기 화면에서)
export function getStudyDetailForApply(studyId: number) {
  return api.get<StudyApplyDetailResDto>(`/api/studies/${studyId}`);
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
