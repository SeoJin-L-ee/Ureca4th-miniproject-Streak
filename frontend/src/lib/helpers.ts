import { CURRENT_USER, assignments, exploreStudies, members, rounds, studies, submissions } from "../data/mock";
import type { Assignment, Round, Study } from "../types";

export function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${d.getMonth() + 1}.${d.getDate()}(${days[d.getDay()]})`;
}

export function formatDateTime(dateStr: string) {
  const d = new Date(dateStr);
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${d.getMonth() + 1}.${d.getDate()}(${days[d.getDay()]}) ${hh}:${mm}`;
}

export function myMemberships() {
  return members.filter((m) => m.userId === CURRENT_USER.id);
}

export function myStudies(): Study[] {
  const ids = myMemberships().map((m) => m.studyId);
  return studies.filter((s) => ids.includes(s.id));
}

export function roleInStudy(studyId: string): "leader" | "member" | null {
  const m = members.find((mm) => mm.studyId === studyId && mm.userId === CURRENT_USER.id);
  return m ? m.role : null;
}

export function myMemberIdInStudy(studyId: string): string | undefined {
  return members.find((m) => m.studyId === studyId && m.userId === CURRENT_USER.id)?.id;
}

export function nextRoundOf(studyId: string): Round | undefined {
  return rounds
    .filter((r) => r.studyId === studyId && r.status === "예정")
    .sort((a, b) => a.date.localeCompare(b.date))[0];
}

export function studyRounds(studyId: string): Round[] {
  return rounds.filter((r) => r.studyId === studyId).sort((a, b) => a.index - b.index);
}

export function studyMembers(studyId: string) {
  return members.filter((m) => m.studyId === studyId);
}

export function myAverageAttendance(): number {
  const mine = myMemberships();
  if (mine.length === 0) return 0;
  return Math.round(mine.reduce((sum, m) => sum + m.attendanceRate, 0) / mine.length);
}

export function upcomingAssignmentsForMe(limit = 3): Assignment[] {
  const myStudyIds = myMemberships().map((m) => m.studyId);
  const now = new Date("2026-07-24T09:00:00");
  return assignments
    .filter((a) => myStudyIds.includes(a.studyId) && new Date(a.deadline) >= now)
    .sort((a, b) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime())
    .slice(0, limit);
}

export function daysUntil(dateStr: string): number {
  const now = new Date("2026-07-24T09:00:00");
  const target = new Date(dateStr);
  const diff = Math.ceil((target.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  return diff;
}

export function todayRounds(): Round[] {
  const today = "2026-07-24";
  const myStudyIds = myMemberships().map((m) => m.studyId);
  return rounds.filter((r) => myStudyIds.includes(r.studyId) && r.date === today);
}

export function studyById(id: string): Study | undefined {
  return studies.find((s) => s.id === id) ?? exploreStudies.find((s) => s.id === id);
}

export function mySubmissionStatus(assignmentId: string, memberId: string | undefined) {
  if (!memberId) return "미제출";
  return submissions.find((s) => s.assignmentId === assignmentId && s.memberId === memberId)?.status ?? "미제출";
}
