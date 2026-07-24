import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Users, TrendingUp, Flame, Clock, CalendarClock, LogOut, ChevronRight } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import StatTile from "../components/StatTile";
import Avatar from "../components/Avatar";
import Badge from "../components/Badge";
import { useAuth } from "../context/AuthContext";
import * as mypageApi from "../api/mypage";
import type { MyPageDashboardResDto } from "../api/types";
import type { ApplicationStatus } from "../api/types";
import { applicationStatusLabel, applicationStatusTone } from "../lib/labels";
import { daysUntil, formatDateTime, formatDday, formatTime } from "../lib/format";

const tabs: ApplicationStatus[] = ["PENDING", "APPROVED", "REJECTED"];

export default function MyPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<ApplicationStatus>("PENDING");
  const [data, setData] = useState<MyPageDashboardResDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    mypageApi
      .getDashboard()
      .then(setData)
      .catch(() => setError("마이페이지 정보를 불러오지 못했어요."))
      .finally(() => setLoading(false));
  }, []);

  const onLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  if (loading) return <PageState message="불러오는 중..." />;
  if (error || !data) return <PageState message={error ?? "데이터가 없어요."} />;

  // 종료(ENDED)된 스터디는 마이페이지 요약에서 제외
  const activeStudies = data.studies.filter((s) => s.status !== "ENDED");
  const leaderCount = activeStudies.filter((s) => s.role === "LEADER").length;
  const filteredApplications = data.applications.filter((a) => a.status === tab);

  return (
    <>
      <Topbar title="마이페이지" />
      <main className="flex-1 space-y-6 p-6">
        <Card>
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <Avatar name={user?.name ?? "?"} size={56} />
              <div>
                <p className="text-lg font-bold text-gray-900">{user?.name}</p>
                <p className="text-sm text-gray-500">{user?.email}</p>
              </div>
            </div>
            <div className="flex gap-8 text-center">
              <div>
                <p className="text-xs text-gray-400">참여 스터디</p>
                <p className="text-xl font-bold text-gray-900">{activeStudies.length}개</p>
              </div>
              <div>
                <p className="text-xs text-gray-400">전체 출석률</p>
                <p className="text-xl font-bold text-emerald-600">{data.attendanceRate.attendanceRate}%</p>
              </div>
              <div>
                <p className="text-xs text-gray-400">스터디장 활동</p>
                <p className="text-xl font-bold text-gray-900">{leaderCount}개</p>
              </div>
            </div>
            <button
              onClick={onLogout}
              className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-500 hover:bg-gray-50"
            >
              <LogOut size={14} /> 로그아웃
            </button>
          </div>
          <div className="mt-5 grid grid-cols-1 gap-3 border-t border-gray-100 pt-5 sm:grid-cols-2">
            <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-2.5">
              <span className="text-sm text-gray-500">전화번호</span>
              <span className="text-sm font-medium text-gray-800">{user?.phone ?? "-"}</span>
            </div>
          </div>
        </Card>

        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-500">1. 상단 개인 요약</h3>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatTile
              icon={<Users size={20} className="text-brand-600" />}
              label="참여중인 스터디"
              value={`${activeStudies.length}개`}
              sub={`리더 ${leaderCount}개 · 참여 ${activeStudies.length - leaderCount}개`}
            />
            <StatTile
              icon={<TrendingUp size={20} className="text-emerald-600" />}
              label="평균 출석률"
              value={`${data.attendanceRate.attendanceRate}%`}
              sub={`총 ${data.attendanceRate.totalCount}회 중 ${data.attendanceRate.presentCount}회 출석`}
              iconBg="bg-emerald-50"
            />
            <StatTile
              icon={<Flame size={20} className="text-orange-500" />}
              label="Streak"
              value={`${data.streak.longestStreak}회`}
              sub="최장 연속 출석"
              iconBg="bg-orange-50"
            />
          </div>
        </section>

        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-500">2. 오늘의 할 일</h3>
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card title="마감 임박 과제 TOP 3">
              <ul className="space-y-3">
                {data.deadlineAssignments.length === 0 && <p className="text-sm text-gray-400">마감 임박 과제가 없어요.</p>}
                {data.deadlineAssignments.slice(0, 3).map((a) => {
                  const d = daysUntil(a.dueAt);
                  return (
                    <li key={a.assignmentId}>
                      <Link
                        to={`/studies/${a.studyId}`}
                        className="flex items-center justify-between rounded-xl border border-gray-100 px-3 py-2.5 hover:bg-gray-50"
                      >
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-gray-800">{a.title}</p>
                          <p className="truncate text-xs text-gray-400">
                            {a.studyTitle} · {formatDateTime(a.dueAt)}
                          </p>
                        </div>
                        <Badge tone={d <= 1 ? "red" : d <= 3 ? "amber" : "gray"}>{formatDday(d)}</Badge>
                      </Link>
                    </li>
                  );
                })}
              </ul>
              <Link to="/studies" className="mt-3 flex items-center justify-center gap-1 text-sm font-medium text-brand-600">
                과제 전체 보기 <ChevronRight size={14} />
              </Link>
            </Card>

            <Card title="오늘 예정 회차">
              <ul className="space-y-3">
                {data.todaySessions.length === 0 && (
                  <p className="flex items-center gap-2 text-sm text-gray-400">
                    <Clock size={16} /> 오늘 예정된 회차가 없어요.
                  </p>
                )}
                {data.todaySessions.map((s) => (
                  <li key={s.sessionId}>
                    <Link
                      to={`/studies/${s.studyId}`}
                      className="flex items-center justify-between rounded-xl border border-gray-100 px-3 py-2.5 hover:bg-gray-50"
                    >
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-gray-800">
                          {s.studyTitle} · {s.title}
                        </p>
                        <p className="truncate text-xs text-gray-400">{formatTime(s.startsAt)}</p>
                      </div>
                      <CalendarClock size={16} className="shrink-0 text-brand-500" />
                    </Link>
                  </li>
                ))}
              </ul>
              <Link to="/members/me/calendar" className="mt-3 flex items-center justify-center gap-1 text-sm font-medium text-brand-600">
                캘린더 전체 보기 <ChevronRight size={14} />
              </Link>
            </Card>
          </div>
        </section>

        <section>
          <h3 className="mb-3 text-sm font-semibold text-gray-500">3. 나의 지원 현황</h3>
          <Card>
            <div className="mb-4 flex gap-1 rounded-xl bg-gray-50 p-1">
              {tabs.map((t) => (
                <button
                  key={t}
                  onClick={() => setTab(t)}
                  className={`flex-1 rounded-lg py-1.5 text-sm font-medium transition-colors ${
                    tab === t ? "bg-white text-brand-700 shadow-sm" : "text-gray-500 hover:text-gray-700"
                  }`}
                >
                  {applicationStatusLabel[t]} ({data.applications.filter((a) => a.status === t).length})
                </button>
              ))}
            </div>
            <ul className="divide-y divide-gray-100">
              {filteredApplications.length === 0 && (
                <p className="py-6 text-center text-sm text-gray-400">해당 상태의 지원 내역이 없어요.</p>
              )}
              {filteredApplications.map((a) => (
                <li key={a.applicationId} className="flex items-center justify-between py-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-gray-800">{a.studyTitle}</p>
                    <p className="truncate text-xs text-gray-400">{formatDateTime(a.appliedAt)} 지원</p>
                  </div>
                  <Badge tone={applicationStatusTone[a.status]}>{applicationStatusLabel[a.status]}</Badge>
                </li>
              ))}
            </ul>
          </Card>
        </section>
      </main>
    </>
  );
}

function PageState({ message }: { message: string }) {
  return (
    <>
      <Topbar title="마이페이지" />
      <main className="flex flex-1 items-center justify-center p-6 text-sm text-gray-400">{message}</main>
    </>
  );
}
