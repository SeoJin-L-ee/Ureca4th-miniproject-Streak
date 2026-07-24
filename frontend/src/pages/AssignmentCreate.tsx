import { useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import * as assignmentsApi from "../api/assignments";

export default function AssignmentCreate() {
  const { studyId, roundId } = useParams<{ studyId: string; roundId: string }>();
  const navigate = useNavigate();
  const sId = Number(studyId);
  const seId = Number(roundId);
  const [form, setForm] = useState({ title: "", description: "", dueAt: "" });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!studyId || !roundId || !Number.isFinite(sId) || !Number.isFinite(seId)) return <Navigate to="/studies" replace />;

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await assignmentsApi.createAssignment(sId, seId, form);
      navigate(`/studies/${studyId}/sessions/${roundId}`);
    } catch {
      setError("과제 생성에 실패했어요.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to={`/studies/${studyId}/sessions/${roundId}`} className="mr-2 text-gray-400 hover:text-gray-600">
            회차 상세 /
          </Link>
        }
        title="과제 생성"
      />
      <main className="flex-1 p-6">
        <Card className="mx-auto max-w-xl">
          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">과제 제목</label>
              <input
                required
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="예: BFS/DFS 보고서"
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">과제 설명</label>
              <textarea
                required
                rows={5}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="과제 상세 내용을 입력하세요"
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">마감 일시</label>
              <input
                required
                type="datetime-local"
                value={form.dueAt}
                onChange={(e) => setForm({ ...form, dueAt: e.target.value })}
                className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
              />
            </div>
            {error && <p className="text-sm text-red-500">{error}</p>}
            <div className="flex gap-2 pt-2">
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
              >
                {saving ? "생성 중..." : "과제 생성"}
              </button>
              <Link
                to={`/studies/${studyId}/sessions/${roundId}`}
                className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                취소
              </Link>
            </div>
          </form>
        </Card>
      </main>
    </>
  );
}
