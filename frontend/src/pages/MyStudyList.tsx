import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronRight } from "lucide-react";
import Topbar from "../components/Topbar";
import Badge from "../components/Badge";
import ProgressBar from "../components/ProgressBar";
import * as studiesApi from "../api/studies";
import type { StudySummaryResDto } from "../api/types";
import { categoryLabel } from "../lib/labels";

export default function MyStudyList() {
  const [studies, setStudies] = useState<StudySummaryResDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    studiesApi
      .getMyStudyList()
      .then((res) => setStudies(res.studyList))
      .catch(() => setError("스터디 목록을 불러오지 못했어요."))
      .finally(() => setLoading(false));
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
    </>
  );
}
