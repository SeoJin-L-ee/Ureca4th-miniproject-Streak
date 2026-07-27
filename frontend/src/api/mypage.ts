import { api } from "./client";
import type { ApplicationStatus, MyApplicationResDto, MyPageDashboardResDto } from "./types";

export function getDashboard() {
  return api.get<MyPageDashboardResDto>("/api/members/me/dashboard");
}

export function getMyApplications(status?: ApplicationStatus) {
  const query = status ? `?status=${status}` : "";
  return api.get<MyApplicationResDto[]>(`/api/members/me/applications${query}`);
}
