import { useEffect, useState } from "react";
import { Search, Users, CalendarClock } from "lucide-react";
import Topbar from "../components/Topbar";
import Badge from "../components/Badge";
import Modal from "../components/Modal";
import * as studiesApi from "../api/studies";
import * as applicationsApi from "../api/applications";
import type { StudyApplyDetailResDto, StudyApplySummaryResDto, StudyCategory } from "../api/types";
import { categoryLabel, studyStatusLabel, studyStatusTone } from "../lib/labels";
import { formatYmd } from "../lib/format";

const categoryOptions: { value: StudyCategory | "ALL"; label: string }[] = [
  { value: "ALL", label: "전체" },
  ...(Object.entries(categoryLabel) as [StudyCategory, string][]).map(([value, label]) => ({ value, label })),
];

const statusFilters: ("RECRUITING" | "CLOSED")[] = ["RECRUITING", "CLOSED"];

export default function StudyExplore() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState<StudyCategory | "ALL">("ALL");
  const [statusFilter, setStatusFilter] = useState<"RECRUITING" | "CLOSED">("RECRUITING");
  const [studies, setStudies] = useState<StudyApplySummaryResDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [detail, setDetail] = useState<StudyApplyDetailResDto | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [applying, setApplying] = useState(false);
  const [applyError, setApplyError] = useState<string | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true);
      studiesApi
        .getStudiesForApply({ category: category === "ALL" ? undefined : category, title: query || undefined })
        .then((res) => setStudies(res.studyList))
        .catch(() => setError("스터디 목록을 불러오지 못했어요."))
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(timer);
  }, [category, query]);

  useEffect(() => {
    if (selectedId == null) return;
    setDetailLoading(true);
    setMessage("");
    setApplyError(null);
    studiesApi
      .getStudyDetailForApply(selectedId)
      .then(setDetail)
      .finally(() => setDetailLoading(false));
  }, [selectedId]);

  const apply = async () => {
    if (!detail) return;
    setApplying(true);
    setApplyError(null);
    try {
      await applicationsApi.applyToStudy(detail.studyId, message);
      setDetail({ ...detail, myStatus: "PENDING" });
      setStudies((prev) => prev.map((s) => (s.studyId === detail.studyId ? { ...s, myStatus: "PENDING" } : s)));
    } catch {
      setApplyError("지원에 실패했어요. 잠시 후 다시 시도해주세요.");
    } finally {
      setApplying(false);
    }
  };

  const visibleStudies = studies.filter((s) => s.status === statusFilter);

  return (
    <>
      <Topbar title="스터디 찾기" />
      <main className="flex-1 space-y-5 p-6">
        <div className="flex gap-1 rounded-xl bg-gray-50 p-1 sm:w-64">
          {statusFilters.map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={`flex-1 rounded-lg py-1.5 text-sm font-medium transition-colors ${
                statusFilter === s ? "bg-white text-brand-700 shadow-sm" : "text-gray-500 hover:text-gray-700"
              }`}
            >
              {studyStatusLabel[s]}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[220px]">
            <Search size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="스터디 이름으로 검색"
              className="w-full rounded-xl border border-gray-200 bg-white py-2.5 pl-9 pr-3 text-sm focus:border-brand-400 focus:outline-none"
            />
          </div>
          <div className="flex flex-wrap gap-1.5">
            {categoryOptions.map((c) => (
              <button
                key={c.value}
                onClick={() => setCategory(c.value)}
                className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                  category === c.value ? "bg-brand-600 text-white" : "bg-white text-gray-500 border border-gray-200 hover:bg-gray-50"
                }`}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>

        {loading && <p className="text-sm text-gray-400">불러오는 중...</p>}
        {error && <p className="text-sm text-red-500">{error}</p>}

        {!loading && !error && (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {visibleStudies.map((s) => (
              <button
                key={s.studyId}
                onClick={() => setSelectedId(s.studyId)}
                className="flex flex-col rounded-2xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-shadow hover:shadow-md"
              >
                <div className="mb-2 flex items-center justify-between">
                  <Badge tone="brand">{categoryLabel[s.category]}</Badge>
                  {s.myStatus === "PENDING" && <Badge tone="amber">지원 검토중</Badge>}
                  {s.myStatus === "ACCEPTED" && <Badge tone="green">참여중</Badge>}
                  {s.myStatus === "REJECTED" && <Badge tone="red">거절됨</Badge>}
                </div>
                <h3 className="mb-1 text-base font-bold text-gray-900">{s.title}</h3>
                <p className="mb-3 text-sm text-gray-500">스터디장 {s.leaderName}</p>
                <div className="mt-auto space-y-1.5 border-t border-gray-100 pt-3 text-xs text-gray-400">
                  <p className="flex items-center gap-1.5">
                    <CalendarClock size={13} /> {formatYmd(s.createdAt)} 개설
                  </p>
                  <p className="flex items-center gap-1.5">
                    <Users size={13} /> {s.currentParticipantCnt}/{s.capacity}명
                  </p>
                </div>
              </button>
            ))}
            {visibleStudies.length === 0 && <p className="col-span-full py-16 text-center text-sm text-gray-400">검색 결과가 없어요.</p>}
          </div>
        )}
      </main>

      <Modal open={selectedId != null} onClose={() => setSelectedId(null)} title={detail?.title}>
        {detailLoading || !detail ? (
          <p className="py-8 text-center text-sm text-gray-400">불러오는 중...</p>
        ) : (
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              <Badge tone="brand">{categoryLabel[detail.category]}</Badge>
              <Badge tone={studyStatusTone[detail.status]}>{studyStatusLabel[detail.status]}</Badge>
            </div>
            <p className="text-sm leading-relaxed text-gray-600">{detail.description}</p>
            <div className="grid grid-cols-1 gap-2 rounded-xl bg-gray-50 p-4 text-sm sm:grid-cols-2">
              <p className="flex items-center gap-1.5 text-gray-600">스터디장 {detail.leaderName}</p>
              <p className="flex items-center gap-1.5 text-gray-600">
                <Users size={14} /> {detail.currentParticipantCnt}/{detail.capacity}명
              </p>
            </div>

            {detail.isLeader ? (
              <div className="rounded-xl bg-gray-50 px-4 py-3 text-center text-sm font-medium text-gray-500">
                내가 스터디장인 스터디예요.
              </div>
            ) : detail.myStatus === "ACCEPTED" ? (
              <div className="rounded-xl bg-emerald-50 px-4 py-3 text-center text-sm font-medium text-emerald-700">
                이미 참여 중인 스터디예요.
              </div>
            ) : detail.myStatus === "PENDING" ? (
              <div className="rounded-xl bg-amber-50 px-4 py-3 text-center text-sm font-medium text-amber-700">
                지원 검토 중이에요. 결과를 기다려주세요.
              </div>
            ) : detail.myStatus === "REJECTED" ? (
              <div className="rounded-xl bg-red-50 px-4 py-3 text-center text-sm font-medium text-red-600">
                아쉽게도 지원이 거절됐어요.
              </div>
            ) : detail.status !== "RECRUITING" ? (
              <div className="rounded-xl bg-gray-50 px-4 py-3 text-center text-sm font-medium text-gray-500">
                모집이 종료된 스터디예요.
              </div>
            ) : detail.currentParticipantCnt >= detail.capacity ? (
              <div className="rounded-xl bg-gray-50 px-4 py-3 text-center text-sm font-medium text-gray-500">정원이 가득 찼어요.</div>
            ) : (
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">지원 메시지</label>
                <textarea
                  rows={3}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="간단한 자기소개나 지원 동기를 남겨주세요"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:border-brand-400 focus:outline-none"
                />
                {applyError && <p className="text-sm text-red-500">{applyError}</p>}
                <button
                  onClick={apply}
                  disabled={applying || !message.trim()}
                  className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
                >
                  {applying ? "지원 중..." : "이 스터디에 지원하기"}
                </button>
              </div>
            )}
          </div>
        )}
      </Modal>
    </>
  );
}
