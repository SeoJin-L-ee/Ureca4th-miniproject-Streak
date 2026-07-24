import { useEffect, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { ChevronRight, Pencil, Plus, Trash2 } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import * as studiesApi from "../api/studies";
import * as sessionsApi from "../api/sessions";
import type { StudyDashboardResDto } from "../api/types";
import { formatDateTime } from "../lib/format";

function toDatetimeLocal(iso: string) {
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export default function RoundList() {
  const { studyId } = useParams<{ studyId: string }>();
  const id = Number(studyId);
  const [data, setData] = useState<StudyDashboardResDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ sessionNumber: 1, title: "", content: "", startsAt: "" });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({ sessionNumber: 1, title: "", content: "", startsAt: "" });
  const [editSaving, setEditSaving] = useState(false);

  const load = () => {
    setLoading(true);
    studiesApi
      .getStudyDashboard(id)
      .then((res) => {
        setData(res);
        setForm((f) => ({ ...f, sessionNumber: res.sessions.totalElements + 1 }));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (Number.isFinite(id)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!studyId || !Number.isFinite(id)) return <Navigate to="/studies" replace />;
  if (loading || !data) {
    return (
      <>
        <Topbar title="회차 목록" />
        <main className="flex flex-1 items-center justify-center p-6 text-sm text-gray-400">불러오는 중...</main>
      </>
    );
  }

  const isLeader = data.isLeader;

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await sessionsApi.createSession(id, form);
      setShowForm(false);
      setForm({ sessionNumber: form.sessionNumber + 1, title: "", content: "", startsAt: "" });
      load();
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (r: StudyDashboardResDto["sessions"]["sessionList"][number]) => {
    setEditingId(r.sessionId);
    setEditForm({ sessionNumber: r.sessionNumber, title: r.title, content: "", startsAt: toDatetimeLocal(r.startsAt) });
  };

  const cancelEdit = () => setEditingId(null);

  const submitEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (editingId == null) return;
    setEditSaving(true);
    try {
      await sessionsApi.updateSession(id, editingId, {
        sessionNumber: editForm.sessionNumber,
        title: editForm.title,
        startsAt: editForm.startsAt,
      });
      setEditingId(null);
      load();
    } finally {
      setEditSaving(false);
    }
  };

  const handleDelete = async (sessionId: number) => {
    if (!window.confirm("이 회차를 삭제할까요? 관련 출석/과제 기록도 함께 삭제됩니다.")) return;
    await sessionsApi.deleteSession(id, sessionId);
    load();
  };

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to={`/studies/${studyId}`} className="mr-2 text-gray-400 hover:text-gray-600">
            {data.title} /
          </Link>
        }
        title="회차 목록"
      />
      <main className="flex-1 space-y-4 p-6">
        {isLeader && (
          <Card>
            {!showForm ? (
              <button
                onClick={() => setShowForm(true)}
                className="flex items-center gap-1.5 rounded-lg bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700"
              >
                <Plus size={15} /> 회차 추가
              </button>
            ) : (
              <form onSubmit={submit} className="grid grid-cols-1 gap-3 sm:grid-cols-4">
                <input
                  required
                  type="number"
                  min={1}
                  placeholder="회차 번호"
                  value={form.sessionNumber}
                  onChange={(e) => setForm({ ...form, sessionNumber: Number(e.target.value) })}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
                <input
                  required
                  placeholder="회차 제목 (예: DP 심화)"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm sm:col-span-2"
                />
                <input
                  required
                  type="datetime-local"
                  value={form.startsAt}
                  onChange={(e) => setForm({ ...form, startsAt: e.target.value })}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
                <textarea
                  placeholder="회차 내용"
                  value={form.content}
                  onChange={(e) => setForm({ ...form, content: e.target.value })}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm sm:col-span-4"
                  rows={2}
                />
                <div className="flex gap-2 sm:col-span-2">
                  <button
                    type="submit"
                    disabled={saving}
                    className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
                  >
                    {saving ? "저장 중..." : "저장"}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowForm(false)}
                    className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                  >
                    취소
                  </button>
                </div>
              </form>
            )}
          </Card>
        )}

        <Card title={`전체 회차 (${data.sessions.sessionList.length})`}>
          <ul className="divide-y divide-gray-100">
            {data.sessions.sessionList.map((r) =>
              editingId === r.sessionId ? (
                <li key={r.sessionId} className="py-3">
                  <form onSubmit={submitEdit} className="grid grid-cols-1 gap-2 rounded-lg border border-gray-200 p-3 sm:grid-cols-4">
                    <input
                      required
                      type="number"
                      min={1}
                      placeholder="회차 번호"
                      value={editForm.sessionNumber}
                      onChange={(e) => setEditForm({ ...editForm, sessionNumber: Number(e.target.value) })}
                      className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                    />
                    <input
                      required
                      placeholder="회차 제목"
                      value={editForm.title}
                      onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
                      className="rounded-lg border border-gray-200 px-3 py-2 text-sm sm:col-span-2"
                    />
                    <input
                      required
                      type="datetime-local"
                      value={editForm.startsAt}
                      onChange={(e) => setEditForm({ ...editForm, startsAt: e.target.value })}
                      className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                    />
                    <div className="flex gap-2 sm:col-span-4">
                      <button
                        type="submit"
                        disabled={editSaving}
                        className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
                      >
                        {editSaving ? "저장 중..." : "저장"}
                      </button>
                      <button
                        type="button"
                        onClick={cancelEdit}
                        className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                      >
                        취소
                      </button>
                    </div>
                  </form>
                </li>
              ) : (
                <li key={r.sessionId} className="flex items-center gap-2 py-1">
                  <Link
                    to={`/studies/${studyId}/sessions/${r.sessionId}`}
                    className="flex flex-1 items-center justify-between rounded-lg py-2 hover:bg-gray-50"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-800">
                        {r.sessionNumber}회차 · {r.title}
                      </p>
                      <p className="text-xs text-gray-400">{formatDateTime(r.startsAt)}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge tone={r.myAttendanceStatus === "UNMARKED" ? "brand" : "gray"}>
                        {r.myAttendanceStatus === "UNMARKED" ? "예정" : "완료"}
                      </Badge>
                      <ChevronRight size={16} className="text-gray-300" />
                    </div>
                  </Link>
                  {isLeader && (
                    <div className="flex shrink-0 items-center gap-1">
                      <button
                        onClick={() => startEdit(r)}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                        aria-label="회차 수정"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => handleDelete(r.sessionId)}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500"
                        aria-label="회차 삭제"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  )}
                </li>
              )
            )}
          </ul>
        </Card>
      </main>
    </>
  );
}
