import { useMemo, useState } from "react";
import { AlertTriangle, Search, Users, CalendarClock, MapPin } from "lucide-react";
import Topbar from "../components/Topbar";
import Badge from "../components/Badge";
import Modal from "../components/Modal";
import { exploreStudies } from "../data/mock";
import type { Study } from "../types";

const categories = ["전체", ...Array.from(new Set(exploreStudies.map((s) => s.category)))];

export default function StudyExplore() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("전체");
  const [selected, setSelected] = useState<Study | null>(null);
  const [applied, setApplied] = useState<string[]>([]);
  const [message, setMessage] = useState("");

  const filtered = useMemo(
    () =>
      exploreStudies.filter(
        (s) =>
          (category === "전체" || s.category === category) &&
          (s.title.includes(query) || s.description.includes(query))
      ),
    [query, category]
  );

  const apply = () => {
    if (!selected) return;
    setApplied((prev) => [...prev, selected.id]);
    setSelected(null);
    setMessage("");
  };

  return (
    <>
      <Topbar title="스터디 찾기" />
      <main className="flex-1 space-y-5 p-6">
        <div className="flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700">
          <AlertTriangle size={16} className="mt-0.5 shrink-0" />
          <p>
            이 화면은 백엔드에 미가입 스터디를 둘러보는 API가 아직 없어(<code className="rounded bg-amber-100 px-1">GET /api/studies?status=RECRUITING</code>{" "}
            필요) 데모 데이터로 표시됩니다. 지원 버튼은 화면에서만 동작하며 실제로 저장되지 않아요.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[220px]">
            <Search size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="스터디 이름이나 키워드로 검색"
              className="w-full rounded-xl border border-gray-200 bg-white py-2.5 pl-9 pr-3 text-sm focus:border-brand-400 focus:outline-none"
            />
          </div>
          <div className="flex gap-1.5">
            {categories.map((c) => (
              <button
                key={c}
                onClick={() => setCategory(c)}
                className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                  category === c ? "bg-brand-600 text-white" : "bg-white text-gray-500 border border-gray-200 hover:bg-gray-50"
                }`}
              >
                {c}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((s) => (
            <button
              key={s.id}
              onClick={() => setSelected(s)}
              className="flex flex-col rounded-2xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="mb-2 flex items-center justify-between">
                <Badge tone="brand">{s.category}</Badge>
                {applied.includes(s.id) && <Badge tone="green">지원완료</Badge>}
              </div>
              <h3 className="mb-1 text-base font-bold text-gray-900">{s.title}</h3>
              <p className="mb-3 line-clamp-2 text-sm text-gray-500">{s.description}</p>
              <div className="mt-auto space-y-1.5 border-t border-gray-100 pt-3 text-xs text-gray-400">
                <p className="flex items-center gap-1.5">
                  <CalendarClock size={13} /> {s.schedule}
                </p>
                <p className="flex items-center gap-1.5">
                  <MapPin size={13} /> {s.location}
                </p>
                <p className="flex items-center gap-1.5">
                  <Users size={13} /> 정원 {s.capacity}명
                </p>
              </div>
            </button>
          ))}
          {filtered.length === 0 && <p className="col-span-full py-16 text-center text-sm text-gray-400">검색 결과가 없어요.</p>}
        </div>
      </main>

      <Modal open={!!selected} onClose={() => setSelected(null)} title={selected?.title}>
        {selected && (
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              <Badge tone="brand">{selected.category}</Badge>
              <Badge tone="amber">{selected.status}</Badge>
            </div>
            <p className="text-sm leading-relaxed text-gray-600">{selected.introLong}</p>
            <div className="grid grid-cols-1 gap-2 rounded-xl bg-gray-50 p-4 text-sm sm:grid-cols-2">
              <p className="flex items-center gap-1.5 text-gray-600">
                <CalendarClock size={14} /> {selected.schedule}
              </p>
              <p className="flex items-center gap-1.5 text-gray-600">
                <MapPin size={14} /> {selected.location}
              </p>
              <p className="flex items-center gap-1.5 text-gray-600">
                <Users size={14} /> 정원 {selected.capacity}명
              </p>
            </div>
            {applied.includes(selected.id) ? (
              <div className="rounded-xl bg-emerald-50 px-4 py-3 text-center text-sm font-medium text-emerald-700">
                이미 지원한 스터디예요. 마이페이지에서 지원 현황을 확인하세요.
              </div>
            ) : (
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">지원 메시지 (선택)</label>
                <textarea
                  rows={3}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="간단한 자기소개나 지원 동기를 남겨주세요"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
                />
                <button
                  onClick={apply}
                  className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-700"
                >
                  이 스터디에 지원하기
                </button>
              </div>
            )}
          </div>
        )}
      </Modal>
    </>
  );
}
