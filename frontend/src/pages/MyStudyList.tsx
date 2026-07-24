import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronRight, Plus } from "lucide-react";
import Topbar from "../components/Topbar";
import Badge from "../components/Badge";
import ProgressBar from "../components/ProgressBar";
import Modal from "../components/Modal";
import * as studiesApi from "../api/studies";
import type { StudyCategory, StudySummaryResDto } from "../api/types";
import { categoryLabel } from "../lib/labels";

export default function MyStudyList() {
  const [studies, setStudies] = useState<StudySummaryResDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  const load = () => {
    setLoading(true);
    studiesApi
      .getMyStudyList()
      .then((res) => setStudies(res.studyList))
      .catch(() => setError("스터디 목록을 불러오지 못했어요."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <>
      <Topbar title="내 스터디" />
      <main className="flex-1 space-y-6 p-6">
        <p className="text-sm text-gray-500">내 스터디 ({studies.length})</p>

        {loading && <p className="text-sm text-gray-400">불러오는 중...</p>}
        {error && <p className="text-sm text-red-500">{error}</p>}
        {!loading && !error && studies.length === 0 && (
          <p className="text-sm text-gray-400">아직 참여 중인 스터디가 없어요. 스터디 찾기에서 둘러보세요.</p>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {studies.map((s) => {
            const attendanceRate = Math.round(s.attendanceRate ?? 0);
            return (
              <Link
                key={s.studyId}
                to={`/studies/${s.studyId}`}
                className="group flex flex-col rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
              >
                <div className="mb-2 flex items-center justify-between">
                  <Badge tone={s.isLeader ? "brand" : "gray"}>{s.isLeader ? "스터디장" : "참여자"}</Badge>
                  <ChevronRight size={16} className="text-gray-300 transition-transform group-hover:translate-x-0.5" />
                </div>
                <h3 className="mb-1 text-base font-bold text-gray-900">{s.title}</h3>
                <p className="mb-3 text-sm text-gray-500">{categoryLabel[s.category]}</p>
                <div className="mt-auto space-y-2 border-t border-gray-100 pt-3">
                  <p className="text-xs text-gray-500">{s.nextSessionInfo}</p>
                  <div className="flex items-center gap-2">
                    <ProgressBar value={attendanceRate} />
                    <span className="w-9 shrink-0 text-right text-xs font-semibold text-brand-600">{attendanceRate}%</span>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      </main>

      <button
        onClick={() => setCreateOpen(true)}
        aria-label="스터디 생성"
        className="fixed bottom-8 right-8 flex h-14 w-14 items-center justify-center rounded-full bg-brand-600 text-white shadow-lg hover:bg-brand-700"
      >
        <Plus size={24} />
      </button>

      <CreateStudyModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => {
          setCreateOpen(false);
          load();
        }}
      />
    </>
  );
}

function CreateStudyModal({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState({ title: "", description: "", capacity: 4, category: "ALGORITHM" as StudyCategory });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) setForm({ title: "", description: "", capacity: 4, category: "ALGORITHM" });
  }, [open]);

  const submit = async () => {
    if (!form.title.trim() || !form.description.trim()) return;
    setSaving(true);
    try {
      await studiesApi.createStudy(form);
      onCreated();
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="스터디 생성">
      <div className="space-y-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">스터디명</label>
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
            placeholder="예: 알고리즘 스터디"
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
            placeholder="스터디를 소개해주세요"
          />
        </div>
        <button
          onClick={submit}
          disabled={saving || !form.title.trim() || !form.description.trim()}
          className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {saving ? "생성 중..." : "생성하기"}
        </button>
      </div>
    </Modal>
  );
}
