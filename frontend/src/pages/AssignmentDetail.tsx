import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { CalendarClock, CheckCircle2, Link as LinkIcon, Pencil, Trash2, XCircle } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import * as assignmentsApi from "../api/assignments";
import * as submissionsApi from "../api/submissions";
import * as studiesApi from "../api/studies";
import { useAuth } from "../context/AuthContext";
import type { AssignmentInfoResDto, SubmissionInfoResDto } from "../api/types";
import { daysUntil, formatDateTime } from "../lib/format";

function toDatetimeLocal(iso: string) {
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export default function AssignmentDetail() {
  const { studyId, roundId, assignmentId } = useParams<{ studyId: string; roundId: string; assignmentId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const sId = Number(studyId);
  const seId = Number(roundId);
  const aId = Number(assignmentId);

  const [assignment, setAssignment] = useState<AssignmentInfoResDto | null>(null);
  const [submissions, setSubmissions] = useState<SubmissionInfoResDto[]>([]);
  const [isLeader, setIsLeader] = useState(false);
  const [loading, setLoading] = useState(true);
  const [draft, setDraft] = useState("");
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState(false);
  const [assignmentForm, setAssignmentForm] = useState({ title: "", description: "", dueAt: "" });
  const [assignmentSaving, setAssignmentSaving] = useState(false);

  const load = () => {
    setLoading(true);
    Promise.all([
      assignmentsApi.getAssignmentDetail(sId, seId, aId),
      submissionsApi.listSubmissions(sId, seId, aId),
      studiesApi.getStudyDashboard(sId),
    ]).then(([a, subs, study]) => {
      setAssignment(a);
      setSubmissions(subs.submissions);
      setIsLeader(study.isLeader);
      setLoading(false);
    });
  };

  useEffect(() => {
    if ([sId, seId, aId].every(Number.isFinite)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sId, seId, aId]);

  if (!studyId || !roundId || !assignmentId || ![sId, seId, aId].every(Number.isFinite)) {
    return <Navigate to="/studies" replace />;
  }
  if (loading || !assignment) {
    return (
      <>
        <Topbar title="과제 상세" />
        <main className="flex flex-1 items-center justify-center p-6 text-sm text-gray-400">불러오는 중...</main>
      </>
    );
  }

  const d = daysUntil(assignment.dueAt);
  const mySubmission = submissions.find((s) => s.memberId === user?.memberId);

  const submit = async () => {
    if (!draft.trim()) return;
    setSaving(true);
    try {
      if (mySubmission?.isSubmitted && mySubmission.submissionId) {
        await submissionsApi.updateSubmission(sId, seId, aId, mySubmission.submissionId, draft);
      } else {
        await submissionsApi.createSubmission(sId, seId, aId, draft);
      }
      setDraft("");
      setEditing(false);
      load();
    } finally {
      setSaving(false);
    }
  };

  const startEditAssignment = () => {
    setAssignmentForm({
      title: assignment.title,
      description: assignment.description,
      dueAt: toDatetimeLocal(assignment.dueAt),
    });
    setEditingAssignment(true);
  };

  const submitEditAssignment = async (e: React.FormEvent) => {
    e.preventDefault();
    setAssignmentSaving(true);
    try {
      await assignmentsApi.updateAssignment(sId, seId, aId, assignmentForm);
      setEditingAssignment(false);
      load();
    } finally {
      setAssignmentSaving(false);
    }
  };

  const deleteAssignment = async () => {
    if (!window.confirm("이 과제를 삭제할까요? 제출된 내용도 함께 삭제됩니다.")) return;
    await assignmentsApi.deleteAssignment(sId, seId, aId);
    navigate(`/studies/${studyId}/sessions/${roundId}`);
  };

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to={`/studies/${studyId}/sessions/${roundId}`} className="mr-2 text-gray-400 hover:text-gray-600">
            회차 상세 /
          </Link>
        }
        title="과제 상세"
      />
      <main className="flex-1 space-y-6 p-6">
        <Card>
          {isLeader && editingAssignment ? (
            <form onSubmit={submitEditAssignment} className="space-y-3">
              <input
                required
                value={assignmentForm.title}
                onChange={(e) => setAssignmentForm({ ...assignmentForm, title: e.target.value })}
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
                placeholder="과제 제목"
              />
              <input
                required
                type="datetime-local"
                value={assignmentForm.dueAt}
                onChange={(e) => setAssignmentForm({ ...assignmentForm, dueAt: e.target.value })}
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
              />
              <textarea
                rows={3}
                value={assignmentForm.description}
                onChange={(e) => setAssignmentForm({ ...assignmentForm, description: e.target.value })}
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
                placeholder="과제 내용"
              />
              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={assignmentSaving}
                  className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
                >
                  {assignmentSaving ? "저장 중..." : "저장"}
                </button>
                <button
                  type="button"
                  onClick={() => setEditingAssignment(false)}
                  className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                >
                  취소
                </button>
              </div>
            </form>
          ) : (
            <>
              <div className="mb-2 flex items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <h2 className="text-lg font-bold text-gray-900">{assignment.title}</h2>
                  <Badge tone={d < 0 ? "gray" : d <= 1 ? "red" : "amber"}>{d < 0 ? "마감" : `D-${d}`}</Badge>
                </div>
                {isLeader && (
                  <div className="flex shrink-0 items-center gap-1">
                    <button
                      onClick={startEditAssignment}
                      className="flex items-center gap-1 rounded-lg border border-gray-200 px-2.5 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50"
                    >
                      <Pencil size={13} /> 수정
                    </button>
                    <button
                      onClick={deleteAssignment}
                      className="flex items-center gap-1 rounded-lg border border-gray-200 px-2.5 py-1.5 text-xs font-medium text-red-500 hover:bg-red-50"
                    >
                      <Trash2 size={13} /> 삭제
                    </button>
                  </div>
                )}
              </div>
              <p className="mb-3 flex items-center gap-1 text-xs text-gray-400">
                <CalendarClock size={13} /> 마감 {formatDateTime(assignment.dueAt)}
              </p>
              <p className="whitespace-pre-wrap text-sm leading-relaxed text-gray-600">{assignment.description}</p>
            </>
          )}
        </Card>

        <Card title="내 제출">
          {mySubmission?.isSubmitted && !editing ? (
            <div className="rounded-xl border border-emerald-100 bg-emerald-50 p-4">
              <div className="mb-1 flex items-center justify-between">
                <p className="flex items-center gap-1.5 text-sm font-medium text-emerald-700">
                  <CheckCircle2 size={16} /> 제출 완료
                </p>
                <button
                  onClick={() => {
                    setDraft(mySubmission.content ?? "");
                    setEditing(true);
                  }}
                  className="flex items-center gap-1 text-xs font-medium text-emerald-700 hover:underline"
                >
                  <Pencil size={12} /> 수정
                </button>
              </div>
              <p className="flex items-center gap-1 text-sm text-emerald-700">
                <LinkIcon size={13} /> {mySubmission.content}
              </p>
              <p className="mt-1 text-xs text-emerald-500">{mySubmission.updatedAt && formatDateTime(mySubmission.updatedAt)} 제출됨</p>
            </div>
          ) : (
            <div className="space-y-3">
              <textarea
                rows={3}
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder="제출 링크 또는 내용을 입력하세요 (예: GitHub 링크)"
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
              />
              <div className="flex gap-2">
                <button
                  onClick={submit}
                  disabled={saving}
                  className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
                >
                  {saving ? "제출 중..." : "제출하기"}
                </button>
                {editing && (
                  <button
                    onClick={() => setEditing(false)}
                    className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                  >
                    취소
                  </button>
                )}
              </div>
            </div>
          )}
        </Card>

        {isLeader && (
          <Card title={`제출 현황 (${submissions.filter((s) => s.isSubmitted).length}/${submissions.length})`}>
            <ul className="divide-y divide-gray-100">
              {submissions.map((s) => (
                <li key={s.memberId} className="flex items-center justify-between py-2.5">
                  <div className="flex items-center gap-3">
                    <Avatar name={s.memberName} size={32} />
                    <div>
                      <p className="text-sm font-medium text-gray-800">{s.memberName}</p>
                      {s.isSubmitted && s.content && <p className="max-w-xs truncate text-xs text-gray-400">{s.content}</p>}
                    </div>
                  </div>
                  {s.isSubmitted ? (
                    <span className="flex items-center gap-1 text-xs font-medium text-emerald-600">
                      <CheckCircle2 size={14} /> 제출완료
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-xs font-medium text-gray-400">
                      <XCircle size={14} /> 미제출
                    </span>
                  )}
                </li>
              ))}
            </ul>
          </Card>
        )}
      </main>
    </>
  );
}
