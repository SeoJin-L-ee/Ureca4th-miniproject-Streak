import { api } from "./client";
import type { CalendarMonthResDto } from "./types";

// yearMonth: "yyyy-MM"
export function getMonthSchedules(yearMonth: string) {
  return api.get<CalendarMonthResDto>(`/api/members/me/calendar?month=${yearMonth}`);
}
