import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronLeft, ChevronRight, ClipboardList, Users } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import * as calendarApi from "../api/calendar";
import type { CalendarItemResDto } from "../api/types";
import { formatTime, toLocalDateStr } from "../lib/format";

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

function buildMonthGrid(year: number, month: number) {
  const first = new Date(year, month, 1);
  const startOffset = first.getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells: (Date | null)[] = [];
  for (let i = 0; i < startOffset; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(new Date(year, month, d));
  while (cells.length % 7 !== 0) cells.push(null);
  return cells;
}

export default function CalendarPage() {
  const today = new Date();
  const [cursor, setCursor] = useState(new Date(today.getFullYear(), today.getMonth(), 1));
  const [selectedDate, setSelectedDate] = useState(toLocalDateStr(today));
  const [schedules, setSchedules] = useState<CalendarItemResDto[]>([]);
  const [loading, setLoading] = useState(true);

  const year = cursor.getFullYear();
  const month = cursor.getMonth();
  const cells = useMemo(() => buildMonthGrid(year, month), [year, month]);

  useEffect(() => {
    setLoading(true);
    const yearMonth = `${year}-${String(month + 1).padStart(2, "0")}`;
    calendarApi
      .getMonthSchedules(yearMonth)
      .then((res) => setSchedules(res.schedules))
      .finally(() => setLoading(false));
  }, [year, month]);

  const eventsByDate = useMemo(() => {
    const map: Record<string, CalendarItemResDto[]> = {};
    schedules.forEach((s) => {
      const key = toLocalDateStr(new Date(s.date));
      map[key] = [...(map[key] ?? []), s];
    });
    return map;
  }, [schedules]);

  const selectedEvents = eventsByDate[selectedDate] ?? [];
  const todayStr = toLocalDateStr(today);

  return (
    <>
      <Topbar title="캘린더" />
      <main className="flex-1 space-y-6 p-6">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <Card className="lg:col-span-2">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-base font-bold text-gray-900">
                {year}년 {month + 1}월
              </h3>
              <div className="flex gap-1">
                <button
                  onClick={() => setCursor(new Date(year, month - 1, 1))}
                  className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50"
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  onClick={() => setCursor(new Date(year, month + 1, 1))}
                  className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
            <div className="grid grid-cols-7 gap-1 text-center">
              {WEEKDAYS.map((w) => (
                <div key={w} className="py-1.5 text-xs font-medium text-gray-400">
                  {w}
                </div>
              ))}
              {cells.map((date, i) => {
                if (!date) return <div key={i} />;
                const dateStr = toLocalDateStr(date);
                const events = eventsByDate[dateStr] ?? [];
                const isSelected = dateStr === selectedDate;
                const isToday = dateStr === todayStr;
                return (
                  <button
                    key={i}
                    onClick={() => setSelectedDate(dateStr)}
                    className={`flex h-20 flex-col items-center rounded-xl border p-1.5 text-left transition-colors ${
                      isSelected ? "border-brand-400 bg-brand-50" : "border-transparent hover:bg-gray-50"
                    }`}
                  >
                    <span
                      className={`mb-1 flex h-6 w-6 items-center justify-center rounded-full text-xs ${
                        isToday ? "bg-brand-600 font-bold text-white" : "text-gray-600"
                      }`}
                    >
                      {date.getDate()}
                    </span>
                    <div className="flex w-full flex-1 flex-col gap-0.5 overflow-hidden">
                      {events.slice(0, 2).map((e) => (
                        <span
                          key={`${e.type}-${e.id}`}
                          className={`truncate rounded px-1 text-[10px] ${
                            e.type === "SESSION" ? "bg-brand-100 text-brand-700" : "bg-amber-100 text-amber-700"
                          }`}
                        >
                          {e.studyTitle}
                        </span>
                      ))}
                      {events.length > 2 && <span className="text-[10px] text-gray-400">+{events.length - 2}</span>}
                    </div>
                  </button>
                );
              })}
            </div>
            {loading && <p className="mt-2 text-center text-xs text-gray-300">불러오는 중...</p>}
          </Card>

          <Card title={`${selectedDate} 일정`}>
            <ul className="space-y-3">
              {selectedEvents.length === 0 && <p className="text-sm text-gray-400">선택한 날짜에 예정된 일정이 없어요.</p>}
              {selectedEvents.map((e) => (
                <li key={`${e.type}-${e.id}`}>
                  <Link
                    to={e.type === "SESSION" ? `/studies/${e.studyId}/sessions/${e.id}` : `/studies/${e.studyId}`}
                    className="block rounded-xl border border-gray-100 p-3 hover:bg-gray-50"
                  >
                    <p className="flex items-center gap-1.5 text-sm font-semibold text-gray-800">
                      {e.type === "SESSION" ? (
                        <Users size={13} className="text-brand-500" />
                      ) : (
                        <ClipboardList size={13} className="text-amber-500" />
                      )}
                      {e.studyTitle}
                    </p>
                    <p className="text-xs text-gray-400">
                      {e.title} · {formatTime(e.date)}
                    </p>
                  </Link>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      </main>
    </>
  );
}
