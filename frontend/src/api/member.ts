import { api } from "./client";
import type { MemberResDto, UpdateMemberResDto } from "./types";

export function getMe() {
  return api.get<MemberResDto>("/api/members/me");
}

export function updateMe(payload: { name: string; phone?: string; currentPassword?: string; newPassword?: string }) {
  return api.patch<UpdateMemberResDto>("/api/members/me", payload);
}
