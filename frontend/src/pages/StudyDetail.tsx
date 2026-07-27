import { useEffect, useState, type ReactNode } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { Users, UserCheck, Settings, Plus, CalendarDays, CheckCircle2 } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import Modal from "../components/Modal";
import * as studiesApi from "../api/studies";
import * as attendanceApi from "../api/attendance";
import * as submissionsApi from "../api/submissions";
import { useAuth } from "../context/AuthContext";
import type { AttendanceMemberResDto, StudyCategory, StudyDashboardResDto, StudyStatus } from "../api/types";
import { categoryLabel, studyStatusLabel, studyStatusTone } from "../lib/labels";
import { formatDateTime, formatDday, formatYmd } from "../lib/format";

interface TeamSubmissionStat {
  submitted: number;
  total: number;
}

type DetailTab = "overview" | "sessions" | "members";

const TOGGLEABLE_STATUSES: StudyStatus[] = ["RECRUITING", "CLOSED"];

export default function StudyDetail() {
  const { studyId } = useParams<{ studyId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const id = Number(studyId);
  const [data, setData] = useState<StudyDashboardResDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editOpen, setEditOpen] = useState(false);
  const [statusSaving, setStatusSaving] = useState(false);
  const [tab, setTab] = useState<DetailTab>("overview");
  const [leaderId, setLeaderId] = useState<number | null>(null);
  const [participantStats, setParticipantStats] = useState<AttendanceMemberResDto[] | null>(null);
  const [participantSessionCount, setParticipantSessionCount] = useState(0);
  const [teamSubmissions, setTeamSubmissions] = useState<Record<number, TeamSubmissionStat> | null>(null);

  const load = () => {
    setLoading(true);
    studiesApi
      .getStudyDashboard(id)
      .then(setData)
      .catch(() => setError("스터디 정보를 불러오지 못했어요."))
      .finally(() => setLoading(false));
    studiesApi
      .getStudyDetailForApply(id)
      .then((res) => setLeaderId(res.leaderId))
      .catch(() => setLeaderId(null));
    attendanceApi
      .getMemberAttendances(id)
      .then((res) => {
        setParticipantStats(res.members);
        setParticipantSessionCount(res.totalSessionCount);
      })
      .catch(() => setParticipantStats([]));
  };

  useEffect(() => {
    if (Number.isFinite(id)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // 다음 회차 과제별 팀 전체 제출 현황을 보여주기 위한 집계
  useEffect(() => {
    if (!data || !data.nextSession || data.nextSessionAssignments.length === 0) {
      setTeamSubmissions(null);
      return;
    }
    const nextSessionId = data.nextSession.sessionId;
    Promise.all(
      data.nextSessionAssignments.map((a) => submissionsApi.listSubmissions(id, nextSessionId, a.assignmentId))
    )
      .then((results) => {
        const map: Record<number, TeamSubmissionStat> = {};
        results.forEach((r) => {
          map[r.assignmentId] = {
            submitted: r.submissions.filter((s) => s.isSubmitted).length,
            total: r.submissions.length,
          };
        });
        setTeamSubmissions(map);
      })
      .catch(() => setTeamSubmissions(null));
  }, [data, id]);

  if (!studyId || !Number.isFinite(id)) return <Navigate to="/studies" replace />;
  if (loading) return <PageState studyId={studyId} message="불러오는 중..." />;
  if (error || !data) return <PageState studyId={studyId} message={error ?? "데이터가 없어요."} />;

  const isLeader = data.isLeader;
  const totalAverage = Math.round(data.attendanceComparison.totalAverage ?? 0);
  const myAverage = Math.round(data.attendanceComparison.myAverage ?? 0);
  const totalRate = Math.round(data.assignmentComparison.totalRate ?? 0);
  const myRate = Math.round(data.assignmentComparison.myRate ?? 0);
  const totalSessionCount = data.sessions.sessionList.length;
  const completedSessionCount = data.sessions.sessionList.filter((s) => new Date(s.startsAt) < new Date()).length;

  const updateStatus = async (status: StudyStatus) => {
    setStatusSaving(true);
    try {
      await studiesApi.updateStudyStatus(id, status);
      load();
    } finally {
      setStatusSaving(false);
    }
  };

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to="/studies" className="mr-2 text-gray-400 hover:text-gray-600">
            내 스터디 /
          </Link>
        }
        title={data.title}
      />
      <main className="flex-1 space-y-6 p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div className="mb-2 flex items-center gap-2">
              <Badge tone="brand">{categoryLabel[data.category]}</Badge>
            </div>
            <h2 className="mb-1 text-2xl font-bold text-gray-900">{data.title}</h2>
            <p className="text-sm text-gray-500">{data.description}</p>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-gray-400">
              <span className="flex items-center gap-1">
                <Users size={13} /> {data.currentParticipantCount}/{data.capacity}명
              </span>
              {isLeader ? (
                <div className="inline-flex overflow-hidden rounded-full border border-gray-200 bg-gray-50 p-0.5">
                  {TOGGLEABLE_STATUSES.map((s) => (
                    <button
                      key={s}
                      disabled={statusSaving}
                      onClick={() => updateStatus(s)}
                      className={`rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors disabled:opacity-60 ${
                        data.status === s ? "bg-brand-600 text-white" : "text-gray-500 hover:text-gray-700"
                      }`}
                    >
                      {studyStatusLabel[s]}
                    </button>
                  ))}
                </div>
              ) : (
                <Badge tone={studyStatusTone[data.status]}>{studyStatusLabel[data.status]}</Badge>
              )}
            </div>
          </div>
          {isLeader && (
            <div className="flex shrink-0 flex-wrap gap-2">
              <Link
                to={`/studies/${studyId}/applications`}
                className="relative flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50"
              >
                <UserCheck size={15} /> 지원자 검토
                {data.currentApplicationCnt > 0 && (
                  <span className="absolute -right-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
                    {data.currentApplicationCnt}
                  </span>
                )}
              </Link>
              <button
                onClick={() => setEditOpen(true)}
                className="flex items-center gap-1.5 rounded-lg bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700"
              >
                <Settings size={15} /> 스터디 수정
              </button>
            </div>
          )}
        </div>

        <div className="flex gap-1 border-b border-gray-200">
          <TabButton active={tab === "overview"} onClick={() => setTab("overview")}>
            개요
          </TabButton>
          <TabButton active={tab === "sessions"} onClick={() => setTab("sessions")}>
            회차
          </TabButton>
          <TabButton active={tab === "members"} onClick={() => setTab("members")}>
            참여자
          </TabButton>
        </div>

        {tab === "overview" && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Card title="출석률 비교">
                <CompareBar teamValue={totalAverage} myValue={myAverage} />
              </Card>
              <Card title="과제 제출률 비교">
                <CompareBar teamValue={totalRate} myValue={myRate} />
              </Card>
            </div>

            <Card title="다음 회차 안내">
              {data.nextSession ? (
                <Link
                  to={`/studies/${studyId}/sessions/${data.nextSession.sessionId}`}
                  className="block rounded-xl border border-gray-100 p-4 hover:bg-gray-50"
                >
                  <div className="mb-1 flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-800">
                      {data.nextSession.sessionNumber}회차 · {data.nextSession.title}
                    </span>
                    <Badge tone="brand">진행예정</Badge>
                  </div>
                  <p className="text-xs text-gray-400">{formatDateTime(data.nextSession.startsAt)}</p>
                </Link>
              ) : (
                <p className="text-sm text-gray-400">예정된 회차가 없어요.</p>
              )}

              {data.nextSession && (
                <div className="mt-4 border-t border-gray-100 pt-4">
                  <p className="mb-2 text-xs font-semibold text-gray-500">다음 회차 과제</p>
                  {data.nextSessionAssignments.length === 0 ? (
                    <p className="text-sm text-gray-400">다음 회차에 등록된 과제가 없어요.</p>
                  ) : (
                    <ul className="space-y-2">
                      {data.nextSessionAssignments.map((a) => {
                        const stat = teamSubmissions?.[a.assignmentId];
                        return (
                          <li key={a.assignmentId}>
                            <Link
                              to={`/studies/${studyId}/sessions/${data.nextSession?.sessionId}/assignments/${a.assignmentId}`}
                              className="flex items-center justify-between gap-2 rounded-xl border border-gray-100 px-3 py-2 hover:bg-gray-50"
                            >
                              <span className="flex min-w-0 items-center gap-2">
                                <Badge tone={a.submitted ? "green" : a.daysUntilDue <= 1 ? "red" : "amber"}>
                                  {a.submitted ? "제출완료" : formatDday(a.daysUntilDue)}
                                </Badge>
                                <span className="truncate text-sm text-gray-700">{a.title}</span>
                              </span>
                              <span className="shrink-0 text-xs text-gray-400">
                                {stat ? `${stat.submitted}/${stat.total}명 제출` : "-"}
                              </span>
                            </Link>
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </div>
              )}
            </Card>
          </div>
        )}

        {tab === "sessions" && (
          <div className="space-y-6">
            <p className="text-sm text-gray-500">
              전체 {totalSessionCount}회차 중 <span className="font-semibold text-brand-600">{completedSessionCount}회차</span> 진행
              완료했어요.
            </p>

            <Card
              title="전체 회차"
              action={
                isLeader && (
                  <Link
                    to={`/studies/${studyId}/sessions`}
                    className="flex items-center gap-1 text-xs font-medium text-brand-600"
                  >
                    <Plus size={13} /> 회차 추가
                  </Link>
                )
              }
            >
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead>
                    <tr className="border-b border-gray-100 text-xs text-gray-400">
                      <th className="py-2 font-medium">날짜 - 회차</th>
                      <th className="py-2 font-medium">내 상태</th>
                      <th className="py-2 font-medium">팀 현황</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {data.sessions.sessionList.map((s) => (
                      <tr
                        key={s.sessionId}
                        onClick={() => navigate(`/studies/${studyId}/sessions/${s.sessionId}`)}
                        className="cursor-pointer hover:bg-gray-50"
                      >
                        <td className="py-2.5">
                          <p className="flex items-center gap-1.5 text-xs text-gray-400">
                            <CalendarDays size={13} /> {formatYmd(s.startsAt)}
                          </p>
                          <p className="font-medium text-gray-800">
                            {s.sessionNumber}회차 - {s.title}
                          </p>
                        </td>
                        <td className="py-2.5">
                          {s.myAttendanceStatus === "PRESENT" ? (
                            <span className="flex items-center gap-1 text-xs font-medium text-emerald-600">
                              <CheckCircle2 size={14} /> 출석
                            </span>
                          ) : (
                            <Badge tone={s.myAttendanceStatus === "ABSENT" ? "red" : "gray"}>
                              {s.myAttendanceStatus === "ABSENT" ? "결석" : new Date(s.startsAt) <= new Date() ? "미결" : "예정"}
                            </Badge>
                          )}
                        </td>
                        <td className="py-2.5 text-gray-500">
                          <div className="flex items-center gap-1.5">
                            <span>출석</span>
                            <span className="inline-block w-9 font-medium text-gray-700">
                              {s.teamAttendanceRate != null ? `${Math.round(s.teamAttendanceRate)}%` : "-"}
                            </span>
                            <span>과제 제출</span>
                            <span className="inline-block w-9 font-medium text-gray-700">
                              {s.teamAssignmentSubmissionRate != null ? `${Math.round(s.teamAssignmentSubmissionRate)}%` : "-"}
                            </span>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          </div>
        )}

        {tab === "members" && (
          <Card title={participantStats ? `참여자 ${participantStats.length}명` : "참여자"}>
            {participantStats === null ? (
              <p className="text-sm text-gray-400">불러오는 중...</p>
            ) : participantStats.length === 0 ? (
              <p className="text-sm text-gray-400">참여자 정보를 불러오지 못했어요.</p>
            ) : (
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {participantStats
                  .slice()
                  .sort((a, b) => b.attendedCount - a.attendedCount)
                  .map((p) => {
                    const rate = participantSessionCount === 0 ? 0 : Math.round((p.attendedCount / participantSessionCount) * 100);
                    return (
                      <div key={p.memberId} className="rounded-xl border border-gray-100 p-4">
                        <Avatar name={p.name} size={34} />
                        <p className="mt-2.5 flex items-center gap-1.5 text-sm font-semibold text-gray-900">
                          {p.name}
                          {p.memberId === user?.memberId && <span className="text-xs font-normal text-gray-400">(나)</span>}
                          {p.memberId === leaderId && <Badge tone="brand">스터디장</Badge>}
                        </p>
                        <p className="mt-1 text-xs text-gray-400">
                          전체 출석률 : {p.attendedCount}/{participantSessionCount} ({rate}%)
                        </p>
                      </div>
                    );
                  })}
              </div>
            )}
          </Card>
        )}
      </main>

      <EditStudyModal
        open={editOpen}
        onClose={() => setEditOpen(false)}
        studyId={id}
        currentStatus={data.status}
        initial={{ title: data.title, description: data.description, capacity: data.capacity, category: data.category }}
        onSaved={() => {
          setEditOpen(false);
          load();
        }}
      />
    </>
  );
}

function TabButton({ active, onClick, children }: { active: boolean; onClick: () => void; children: ReactNode }) {
  return (
    <button
      onClick={onClick}
      className={`border-b-2 px-4 py-2.5 text-sm font-semibold transition-colors ${
        active ? "border-brand-600 text-brand-600" : "border-transparent text-gray-400 hover:text-gray-600"
      }`}
    >
      {children}
    </button>
  );
}

function CompareBar({ teamValue, myValue }: { teamValue: number; myValue: number }) {
  return (
    <div className="space-y-3">
      <div className="grid grid-cols-[64px_1fr_38px] items-center gap-3">
        <span className="text-xs text-gray-500">스터디 평균</span>
        <div className="h-2.5 overflow-hidden rounded-full bg-gray-100">
          <div className="h-full rounded-full bg-brand-200" style={{ width: `${teamValue}%` }} />
        </div>
        <span className="text-right text-xs font-bold text-gray-700">{teamValue}%</span>
      </div>
      <div className="grid grid-cols-[64px_1fr_38px] items-center gap-3">
        <span className="text-xs text-gray-500">내 평균</span>
        <div className="h-2.5 overflow-hidden rounded-full bg-gray-100">
          <div className="h-full rounded-full bg-brand-600" style={{ width: `${myValue}%` }} />
        </div>
        <span className="text-right text-xs font-bold text-brand-700">{myValue}%</span>
      </div>
    </div>
  );
}

function EditStudyModal({
  open,
  onClose,
  studyId,
  currentStatus,
  initial,
  onSaved,
}: {
  open: boolean;
  onClose: () => void;
  studyId: number;
  currentStatus: StudyStatus;
  initial: { title: string; description: string; capacity: number; category: StudyCategory };
  onSaved: () => void;
}) {
  const [form, setForm] = useState(initial);
  const [saving, setSaving] = useState(false);
  const [ending, setEnding] = useState(false);

  useEffect(() => {
    if (open) setForm(initial);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const submit = async () => {
    setSaving(true);
    try {
      await studiesApi.updateStudy(studyId, form);
      onSaved();
    } finally {
      setSaving(false);
    }
  };

  const endStudy = async () => {
    if (!window.confirm("정말 스터디를 종료할까요? 종료하면 되돌릴 수 없어요.")) return;
    setEnding(true);
    try {
      await studiesApi.updateStudyStatus(studyId, "ENDED");
      onSaved();
    } finally {
      setEnding(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="스터디 수정">
      <div className="space-y-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">스터디명</label>
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">카테고리</label>
          <select
            value={form.category}
            onChange={(e) => setForm({ ...form, category: e.target.value as StudyCategory })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
          >
            {Object.entries(categoryLabel).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">정원</label>
          <input
            type="number"
            min={1}
            value={form.capacity}
            onChange={(e) => setForm({ ...form, capacity: Number(e.target.value) })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">설명</label>
          <textarea
            rows={3}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
          />
        </div>
        <button
          onClick={submit}
          disabled={saving}
          className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {saving ? "저장 중..." : "저장"}
        </button>
        {currentStatus !== "ENDED" && (
          <button
            onClick={endStudy}
            disabled={ending}
            className="w-full rounded-lg border border-red-200 py-2.5 text-sm font-semibold text-red-600 hover:bg-red-50 disabled:opacity-60"
          >
            {ending ? "종료 처리 중..." : "스터디 종료"}
          </button>
        )}
      </div>
    </Modal>
  );
}

function PageState({ studyId, message }: { studyId: string; message: string }) {
  return (
    <>
      <Topbar
        breadcrumb={
          <Link to="/studies" className="mr-2 text-gray-400 hover:text-gray-600">
            내 스터디 /
          </Link>
        }
        title={`스터디 #${studyId}`}
      />
      <main className="flex flex-1 items-center justify-center p-6 text-sm text-gray-400">{message}</main>
    </>
  );
}
