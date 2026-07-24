import { useEffect, useState } from "react";
import { Bell, CalendarClock, ClipboardList } from "lucide-react";
import Avatar from "./Avatar";
import { useAuth } from "../context/AuthContext";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import * as mypageApi from "../api/mypage";
import type { MyPageDashboardResDto } from "../api/types";
import { daysUntil, formatDday, formatTime } from "../lib/format";

export default function Topbar({ title, breadcrumb }: { title?: string; breadcrumb?: ReactNode }) {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [data, setData] = useState<MyPageDashboardResDto | null>(null);

  useEffect(() => {
    mypageApi
      .getDashboard()
      .then(setData)
      .catch(() => setData(null));
  }, []);

  const deadlines = data?.deadlineAssignments.slice(0, 3) ?? [];
  const todaySessions = data?.todaySessions ?? [];
  const hasNotifications = deadlines.length > 0 || todaySessions.length > 0;

  return (
    <header className="relative flex h-16 shrink-0 items-center justify-between border-b border-gray-200 bg-white/80 px-6 backdrop-blur">
      <div className="text-sm text-gray-500">
        {breadcrumb}
        {title && <span className="text-base font-semibold text-gray-900">{title}</span>}
      </div>
      <div className="flex items-center gap-4">
        <div className="relative">
          <button
            onClick={() => setOpen((v) => !v)}
            className="relative flex h-9 w-9 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-600"
          >
            <Bell size={18} />
            {hasNotifications && <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-red-500" />}
          </button>

          {open && (
            <>
              <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
              <div className="absolute right-0 top-11 z-50 w-80 rounded-2xl border border-gray-200 bg-white p-3 shadow-xl">
                <p className="mb-2 px-1 text-sm font-semibold text-gray-800">알림</p>
                {!hasNotifications && <p className="px-1 py-4 text-center text-sm text-gray-400">새로운 알림이 없어요.</p>}

                {todaySessions.length > 0 && (
                  <div className="mb-2">
                    <p className="px-1 pb-1 text-xs font-medium text-gray-400">오늘 예정 회차</p>
                    <ul className="space-y-1">
                      {todaySessions.map((s) => (
                        <li key={s.sessionId}>
                          <Link
                            to={`/studies/${s.studyId}`}
                            onClick={() => setOpen(false)}
                            className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-sm hover:bg-gray-50"
                          >
                            <CalendarClock size={14} className="shrink-0 text-brand-500" />
                            <span className="min-w-0 flex-1 truncate text-gray-700">
                              {s.studyTitle} · {s.title}
                            </span>
                            <span className="shrink-0 text-xs text-gray-400">{formatTime(s.startsAt)}</span>
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {deadlines.length > 0 && (
                  <div>
                    <p className="px-1 pb-1 text-xs font-medium text-gray-400">마감 임박 과제</p>
                    <ul className="space-y-1">
                      {deadlines.map((a) => (
                        <li key={a.assignmentId}>
                          <Link
                            to={`/studies/${a.studyId}`}
                            onClick={() => setOpen(false)}
                            className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-sm hover:bg-gray-50"
                          >
                            <ClipboardList size={14} className="shrink-0 text-gray-400" />
                            <span className="min-w-0 flex-1 truncate text-gray-700">{a.title}</span>
                            <span className="shrink-0 text-xs font-medium text-amber-600">{formatDday(daysUntil(a.dueAt))}</span>
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </>
          )}
        </div>
        <Link to="/members/me" className="flex items-center gap-2">
          <Avatar name={user?.name ?? "?"} size={32} />
          <span className="text-sm font-medium text-gray-700">{user?.name}</span>
        </Link>
      </div>
    </header>
  );
}
