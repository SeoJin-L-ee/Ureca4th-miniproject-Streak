import { useEffect, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { CalendarClock, Save, Plus, ClipboardList, CheckCircle2, XCircle } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import * as sessionsApi from "../api/sessions";
import * as attendanceApi from "../api/attendance";
import * as studiesApi from "../api/studies";
import { useAuth } from "../context/AuthContext";
import type { AttendanceStatus, SessionInfoResDto } from "../api/types";
import { attendanceStatusLabel, attendanceStatusTone } from "../lib/labels";
import { formatDateTime } from "../lib/format";

const cycle: AttendanceStatus[] = ["UNMARKED", "PRESENT", "ABSENT"];

const attendanceTabs = ["전체", "출석", "결석", "미체크"] as const;
type AttendanceTab = (typeof attendanceTabs)[number];
const tabStatus: Record<Exclude<AttendanceTab, "전체">, AttendanceStatus> = {
  출석: "PRESENT",
  결석: "ABSENT",
  미체크: "UNMARKED",
};

export default function RoundDetail() {
  const { studyId, roundId } = useParams<{ studyId: string; roundId: string }>();
  const { user } = useAuth();
  const sId = Number(studyId);
  const seId = Number(roundId);

  const [session, setSession] = useState<SessionInfoResDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [attendanceDraft, setAttendanceDraft] = useState<Record<number, AttendanceStatus>>({});
  const [saving, setSaving] = useState(false);
  const [isLeader, setIsLeader] = useState(false);
  const [attendanceTab, setAttendanceTab] = useState<AttendanceTab>("전체");

  const load = () => {
    setLoading(true);
    Promise.all([sessionsApi.getSessionDetail(sId, seId), studiesApi.getStudyDashboard(sId)]).then(([res, study]) => {
      setSession(res);
      const draft: Record<number, AttendanceStatus> = {};
      res.attendances.forEach((a) => (draft[a.memberId] = a.status));
      setAttendanceDraft(draft);
      setIsLeader(study.isLeader);
      setLoading(false);
    });
  };

  useEffect(() => {
    if (Number.isFinite(sId) && Number.isFinite(seId)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sId, seId]);

  if (!studyId || !roundId || !Number.isFinite(sId) || !Number.isFinite(seId)) return <Navigate to="/studies" replace />;
  if (loading || !session) {
    return (
      <>
        <Topbar title="회차 상세" />
        <main className="flex flex-1 items-center justify-center p-6 text-sm text-gray-400">불러오는 중...</main>
      </>
    );
  }

  const cycleStatus = (memberId: number) => {
    setAttendanceDraft((prev) => {
      const current = prev[memberId] ?? "UNMARKED";
      const next = cycle[(cycle.indexOf(current) + 1) % cycle.length];
      return { ...prev, [memberId]: next };
    });
  };

  const saveAttendance = async () => {
    setSaving(true);
    try {
      const attendances = Object.entries(attendanceDraft).map(([memberId, status]) => ({
        memberId: Number(memberId),
        status,
      }));
      await attendanceApi.updateSessionAttendances(sId, seId, attendances);
      load();
    } finally {
      setSaving(false);
    }
  };

  const myAttendance = session.attendances.find((a) => a.memberId === user?.memberId);

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to={`/studies/${studyId}`} className="mr-2 text-gray-400 hover:text-gray-600">
            스터디 /
          </Link>
        }
        title={`${session.sessionNumber}회차`}
      />
      <main className="flex-1 space-y-6 p-6">
        <Card>
          <h2 className="mb-1 text-lg font-bold text-gray-900">
            {session.sessionNumber}회차 · {session.title}
          </h2>
          <p className="mb-2 flex items-center gap-1 text-sm text-gray-500">
            <CalendarClock size={14} /> {formatDateTime(session.startsAt)}
          </p>
          {session.content && <p className="whitespace-pre-wrap text-sm text-gray-600">{session.content}</p>}
        </Card>

        {isLeader ? (
          <Card
            title="참여자별 출석"
            action={
              <button
                onClick={saveAttendance}
                disabled={saving}
                className="flex items-center gap-1.5 rounded-lg bg-brand-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-brand-700 disabled:opacity-60"
              >
                <Save size={13} /> {saving ? "저장 중..." : "출결 저장"}
              </button>
            }
          >
            <div className="mb-3 flex gap-1 rounded-xl bg-gray-50 p-1">
              {attendanceTabs.map((t) => {
                const count =
                  t === "전체"
                    ? session.attendances.length
                    : session.attendances.filter((a) => (attendanceDraft[a.memberId] ?? a.status) === tabStatus[t]).length;
                return (
                  <button
                    key={t}
                    onClick={() => setAttendanceTab(t)}
                    className={`flex-1 rounded-lg py-1.5 text-sm font-medium transition-colors ${
                      attendanceTab === t ? "bg-white text-brand-700 shadow-sm" : "text-gray-500 hover:text-gray-700"
                    }`}
                  >
                    {t} ({count})
                  </button>
                );
              })}
            </div>
            <ul className="divide-y divide-gray-100">
              {session.attendances
                .filter((a) => attendanceTab === "전체" || (attendanceDraft[a.memberId] ?? a.status) === tabStatus[attendanceTab])
                .map((a) => {
                  const status = attendanceDraft[a.memberId] ?? a.status;
                  return (
                    <li key={a.memberId} className="flex items-center justify-between py-2.5">
                      <div className="flex items-center gap-3">
                        <Avatar name={a.memberName} size={32} />
                        <p className="text-sm font-medium text-gray-800">{a.memberName}</p>
                      </div>
                      <button onClick={() => cycleStatus(a.memberId)} className="cursor-pointer">
                        <Badge tone={attendanceStatusTone[status]}>{attendanceStatusLabel[status]}</Badge>
                      </button>
                    </li>
                  );
                })}
            </ul>
          </Card>
        ) : (
          <Card title="내 출석 상태">
            <div className="flex items-center justify-between rounded-xl border border-gray-100 p-4">
              <div className="flex items-center gap-3">
                <Avatar name={user?.name ?? "나"} size={36} />
                <p className="text-sm font-medium text-gray-800">{user?.name}</p>
              </div>
              <Badge tone={attendanceStatusTone[myAttendance?.status ?? "UNMARKED"]}>
                {attendanceStatusLabel[myAttendance?.status ?? "UNMARKED"]}
              </Badge>
            </div>
          </Card>
        )}

        <Card
          title="과제 리스트"
          action={
            isLeader && (
              <Link to={`/studies/${studyId}/sessions/${roundId}/assignments/new`} className="flex items-center gap-1 text-xs font-medium text-brand-600">
                <Plus size={13} /> 과제 추가
              </Link>
            )
          }
        >
          <ul className="space-y-2">
            {session.assignments.length === 0 && <p className="text-sm text-gray-400">이 회차에 등록된 과제가 없어요.</p>}
            {session.assignments.map((a) => (
              <li key={a.assignmentId}>
                <Link
                  to={`/studies/${studyId}/sessions/${roundId}/assignments/${a.assignmentId}`}
                  className="flex items-center justify-between rounded-xl border border-gray-100 px-3 py-2.5 hover:bg-gray-50"
                >
                  <span className="flex items-center gap-2 text-sm text-gray-700">
                    <ClipboardList size={14} className="text-gray-400" /> {a.title}
                  </span>
                  {a.isSubmitted ? (
                    <span className="flex items-center gap-1 text-xs font-medium text-emerald-600">
                      <CheckCircle2 size={14} /> 제출완료
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-xs font-medium text-gray-400">
                      <XCircle size={14} /> 미제출
                    </span>
                  )}
                </Link>
              </li>
            ))}
          </ul>
        </Card>
      </main>
    </>
  );
}
