import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { Users, ClipboardList, UserCheck, Settings, Plus, CalendarDays, CheckCircle2, ChevronRight } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import ProgressBar from "../components/ProgressBar";
import MiniBarChart from "../components/MiniBarChart";
import Modal from "../components/Modal";
import * as studiesApi from "../api/studies";
import * as attendanceApi from "../api/attendance";
import * as submissionsApi from "../api/submissions";
import { useAuth } from "../context/AuthContext";
import type { StudyCategory, StudyDashboardResDto, StudyStatus } from "../api/types";
import { categoryLabel, studyStatusLabel, studyStatusTone } from "../lib/labels";
import { formatDateTime, formatDday, formatYmd } from "../lib/format";

interface ParticipantAttendanceStat {
  memberId: number;
  name: string;
  present: number;
  total: number;
}

interface TeamSubmissionStat {
  submitted: number;
  total: number;
}

export default function StudyDetail() {
  const { studyId } = useParams<{ studyId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const id = Number(studyId);
  const [data, setData] = useState<StudyDashboardResDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editOpen, setEditOpen] = useState(false);
  const [attendanceModalOpen, setAttendanceModalOpen] = useState(false);
  const [participantStats, setParticipantStats] = useState<ParticipantAttendanceStat[] | null>(null);
  const [teamSubmissions, setTeamSubmissions] = useState<Record<number, TeamSubmissionStat> | null>(null);

  const load = () => {
    setLoading(true);
    studiesApi
      .getStudyDashboard(id)
      .then(setData)
      .catch(() => setError("스터디 정보를 불러오지 못했어요."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (Number.isFinite(id)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // 회차별 출석 기록을 모아 참여자별 출석 현황을 집계 (전용 집계 API가 없어 클라이언트에서 계산)
  useEffect(() => {
    if (!data) return;
    const sessionIds = data.sessions.sessionList.map((s) => s.sessionId);
    if (sessionIds.length === 0) {
      setParticipantStats([]);
      return;
    }
    Promise.all(sessionIds.map((sid) => attendanceApi.getSessionAttendances(id, sid)))
      .then((results) => {
        const map = new Map<number, ParticipantAttendanceStat>();
        results.forEach((r) => {
          r.participants.forEach((p) => {
            const entry = map.get(p.memberId) ?? { memberId: p.memberId, name: p.name, present: 0, total: 0 };
            if (p.status !== "UNMARKED") {
              entry.total += 1;
              if (p.status === "PRESENT") entry.present += 1;
            }
            map.set(p.memberId, entry);
          });
        });
        setParticipantStats(Array.from(map.values()));
      })
      .catch(() => setParticipantStats([]));
  }, [data, id]);

  // 스터디장에게는 다음 회차 과제별 팀 전체 제출 현황을 보여주기 위한 집계
  useEffect(() => {
    if (!data || !data.isLeader || !data.nextSession || data.nextSessionAssignments.length === 0) {
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
        <Card>
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <div className="mb-2 flex items-center gap-2">
                <Badge tone={isLeader ? "brand" : "gray"}>{isLeader ? "스터디장" : "참여자"}</Badge>
                <Badge tone={studyStatusTone[data.status]}>{studyStatusLabel[data.status]}</Badge>
                <Badge tone="gray">{categoryLabel[data.category]}</Badge>
              </div>
              <h2 className="mb-1 text-xl font-bold text-gray-900">{data.title}</h2>
              <p className="text-sm text-gray-500">{data.description}</p>
              <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-gray-400">
                <span className="flex items-center gap-1">
                  <Users size={13} /> {data.currentParticipantCount}/{data.capacity}명
                </span>
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
        </Card>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
            <p className="mb-1 text-xs text-gray-500">전체 회차</p>
            <p className="text-2xl font-bold text-gray-900">{data.sessions.totalElements}</p>
          </div>
          <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
            <p className="mb-1 text-xs text-gray-500">참여 인원</p>
            <p className="text-2xl font-bold text-gray-900">
              {data.currentParticipantCount}
              <span className="text-base font-medium text-gray-400">명</span>
            </p>
          </div>
          <Card title="출석률 비교">
            <MiniBarChart
              height={120}
              colorLight="#bfdbfe"
              colorDark="#3b82f6"
              data={[
                { label: "스터디 평균", value: totalAverage },
                { label: "내 평균", value: myAverage },
              ]}
            />
          </Card>
          <Card title="과제 제출률 비교">
            <MiniBarChart
              height={120}
              colorLight="#ddd6fe"
              colorDark="#5b3fe0"
              data={[
                { label: "스터디 평균", value: totalRate },
                { label: "내 평균", value: myRate },
              ]}
            />
          </Card>
        </div>

        <Card title="참여자별 출석 현황">
          {participantStats === null ? (
            <p className="text-sm text-gray-400">불러오는 중...</p>
          ) : participantStats.length === 0 ? (
            <p className="text-sm text-gray-400">아직 출석이 기록된 회차가 없어요.</p>
          ) : (
            <button
              onClick={() => setAttendanceModalOpen(true)}
              className="flex w-full items-center justify-between rounded-xl border border-gray-100 px-4 py-3 text-left hover:bg-gray-50"
            >
              <span className="flex items-center gap-2 text-sm text-gray-600">
                <Users size={15} className="text-gray-400" /> 참여자 {participantStats.length}명의 출석 상태를 확인해보세요.
              </span>
              <ChevronRight size={16} className="shrink-0 text-gray-300" />
            </button>
          )}
        </Card>

        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
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
            {data.nextSessionAssignments.length > 0 && (
              <ul className="mt-3 space-y-2 border-t border-gray-100 pt-3">
                {data.nextSessionAssignments.map((a) => (
                  <li key={a.assignmentId} className="flex items-center justify-between text-sm">
                    <span className="truncate text-gray-600">{a.title}</span>
                    <Badge tone={a.submitted ? "green" : "amber"}>{a.submitted ? "제출완료" : formatDday(a.daysUntilDue)}</Badge>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card
            title="과제 제출 현황"
            action={
              data.nextSession &&
              isLeader && (
                <Link
                  to={`/studies/${studyId}/sessions/${data.nextSession.sessionId}/assignments/new`}
                  className="flex items-center gap-1 text-xs font-medium text-brand-600"
                >
                  <Plus size={13} /> 과제 추가
                </Link>
              )
            }
          >
            {isLeader ? (
              <ul className="space-y-3">
                {data.nextSessionAssignments.length === 0 && <p className="text-sm text-gray-400">다음 회차에 등록된 과제가 없어요.</p>}
                {data.nextSessionAssignments.map((a) => {
                  const stat = teamSubmissions?.[a.assignmentId];
                  const rate = stat && stat.total > 0 ? Math.round((stat.submitted / stat.total) * 100) : 0;
                  return (
                    <li key={a.assignmentId}>
                      <Link
                        to={`/studies/${studyId}/sessions/${data.nextSession?.sessionId}/assignments/${a.assignmentId}`}
                        className="block rounded-xl border border-gray-100 px-3 py-2 hover:bg-gray-50"
                      >
                        <div className="mb-1.5 flex items-center justify-between text-sm">
                          <span className="flex items-center gap-2 truncate text-gray-700">
                            <ClipboardList size={14} className="shrink-0 text-gray-400" /> {a.title}
                          </span>
                          <span className="shrink-0 text-xs text-gray-400">{stat ? `${stat.submitted}/${stat.total}명` : "-"}</span>
                        </div>
                        <ProgressBar value={rate} />
                      </Link>
                    </li>
                  );
                })}
              </ul>
            ) : (
              <ul className="space-y-2">
                {data.nextSessionAssignments.length === 0 && <p className="text-sm text-gray-400">다음 회차에 등록된 과제가 없어요.</p>}
                {data.nextSessionAssignments.map((a) => (
                  <li key={a.assignmentId}>
                    <Link
                      to={`/studies/${studyId}/sessions/${data.nextSession?.sessionId}/assignments/${a.assignmentId}`}
                      className="flex items-center justify-between rounded-xl border border-gray-100 px-3 py-2 hover:bg-gray-50"
                    >
                      <span className="flex items-center gap-2 truncate text-sm text-gray-700">
                        <ClipboardList size={14} className="shrink-0 text-gray-400" /> {a.title}
                      </span>
                      <Badge tone={a.submitted ? "green" : a.daysUntilDue <= 1 ? "red" : "amber"}>
                        {a.submitted ? "제출완료" : formatDday(a.daysUntilDue)}
                      </Badge>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>

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
                          {s.myAttendanceStatus === "ABSENT" ? "결석" : "예정"}
                        </Badge>
                      )}
                    </td>
                    <td className="py-2.5 text-gray-500">
                      <span className="mr-4">
                        출석{" "}
                        <span className="font-medium text-gray-700">
                          {s.teamAttendanceRate != null ? `${Math.round(s.teamAttendanceRate)}%` : "-"}
                        </span>
                      </span>
                      <span>
                        과제 제출{" "}
                        <span className="font-medium text-gray-700">
                          {s.teamAssignmentSubmissionRate != null ? `${Math.round(s.teamAssignmentSubmissionRate)}%` : "-"}
                        </span>
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </main>

      <EditStudyModal
        open={editOpen}
        onClose={() => setEditOpen(false)}
        studyId={id}
        initial={{ title: data.title, description: data.description, capacity: data.capacity, category: data.category, status: data.status }}
        onSaved={() => {
          setEditOpen(false);
          load();
        }}
      />

      <Modal open={attendanceModalOpen} onClose={() => setAttendanceModalOpen(false)} title="참여자별 출석 현황">
        <ul className="divide-y divide-gray-100">
          {(participantStats ?? [])
            .slice()
            .sort((a, b) => (b.total === 0 ? 0 : b.present / b.total) - (a.total === 0 ? 0 : a.present / a.total))
            .map((p) => {
              const rate = p.total === 0 ? 0 : Math.round((p.present / p.total) * 100);
              return (
                <li key={p.memberId} className="flex items-center justify-between py-2.5">
                  <div className="flex items-center gap-3">
                    <Avatar name={p.name} size={32} />
                    <p className="text-sm font-medium text-gray-800">
                      {p.name}
                      {p.memberId === user?.memberId && <span className="ml-1 text-xs text-gray-400">(나)</span>}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs text-gray-400">
                      {p.present}/{p.total}
                    </span>
                    <span className="w-10 text-right text-sm font-semibold text-brand-600">{rate}%</span>
                  </div>
                </li>
              );
            })}
        </ul>
      </Modal>
    </>
  );
}

function EditStudyModal({
  open,
  onClose,
  studyId,
  initial,
  onSaved,
}: {
  open: boolean;
  onClose: () => void;
  studyId: number;
  initial: { title: string; description: string; capacity: number; category: StudyCategory; status: StudyStatus };
  onSaved: () => void;
}) {
  const [form, setForm] = useState(initial);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) setForm(initial);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const submit = async () => {
    setSaving(true);
    try {
      const { status, ...studyFields } = form;
      await studiesApi.updateStudy(studyId, studyFields);
      if (status !== initial.status) {
        await studiesApi.updateStudyStatus(studyId, status);
      }
      onSaved();
    } finally {
      setSaving(false);
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
          <label className="mb-1 block text-sm font-medium text-gray-700">모집 상태</label>
          <select
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value as StudyStatus })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
          >
            {Object.entries(studyStatusLabel).map(([value, label]) => (
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
