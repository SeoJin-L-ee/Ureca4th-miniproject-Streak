import { useEffect, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { AlertTriangle, Check, X } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import * as studiesApi from "../api/studies";
import { applications as demoApplications } from "../data/mock";
import type { Application, ApplicationStatus } from "../types";

const tabs: ApplicationStatus[] = ["대기", "승인", "거절"];
const statusTone: Record<ApplicationStatus, "amber" | "green" | "red"> = {
  대기: "amber",
  승인: "green",
  거절: "red",
};

export default function ApplicantManage() {
  const { studyId } = useParams<{ studyId: string }>();
  const id = Number(studyId);
  const [studyTitle, setStudyTitle] = useState<string>("");
  const [apps, setApps] = useState<Application[]>(demoApplications);
  const [tab, setTab] = useState<ApplicationStatus>("대기");

  useEffect(() => {
    if (Number.isFinite(id)) {
      studiesApi
        .getStudyDashboard(id)
        .then((res) => setStudyTitle(res.title))
        .catch(() => setStudyTitle(""));
    }
  }, [id]);

  if (!studyId || !Number.isFinite(id)) return <Navigate to="/studies" replace />;

  const updateStatus = (appId: string, status: ApplicationStatus) => {
    setApps((prev) => prev.map((a) => (a.id === appId ? { ...a, status } : a)));
  };

  const filtered = apps.filter((a) => a.status === tab);

  return (
    <>
      <Topbar
        breadcrumb={
          <Link to={`/studies/${studyId}`} className="mr-2 text-gray-400 hover:text-gray-600">
            {studyTitle || `스터디 #${studyId}`} /
          </Link>
        }
        title="지원자 관리"
      />
      <main className="flex-1 p-6">
        <div className="mb-4 flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700">
          <AlertTriangle size={16} className="mt-0.5 shrink-0" />
          <p>
            이 화면은 백엔드에 지원자 목록 조회 API가 아직 없어(<code className="rounded bg-amber-100 px-1">GET /api/studies/{"{studyId}"}/applications</code>{" "}
            필요) 데모 데이터로 표시됩니다. 승인/거절 버튼은 화면에서만 동작하며 실제로 저장되지 않아요.
          </p>
        </div>
        <Card>
          <div className="mb-4 flex gap-1 rounded-xl bg-gray-50 p-1">
            {tabs.map((t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`flex-1 rounded-lg py-1.5 text-sm font-medium transition-colors ${
                  tab === t ? "bg-white text-brand-700 shadow-sm" : "text-gray-500 hover:text-gray-700"
                }`}
              >
                {t} ({apps.filter((a) => a.status === t).length})
              </button>
            ))}
          </div>

          <ul className="space-y-3">
            {filtered.length === 0 && <p className="py-8 text-center text-sm text-gray-400">해당 상태의 지원자가 없어요.</p>}
            {filtered.map((a) => (
              <li key={a.id} className="rounded-xl border border-gray-100 p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-3">
                    <Avatar name={a.applicantName} size={40} />
                    <div>
                      <div className="mb-1 flex items-center gap-2">
                        <p className="text-sm font-semibold text-gray-800">{a.applicantName}</p>
                        <Badge tone={statusTone[a.status]}>{a.status}</Badge>
                      </div>
                      <p className="text-sm text-gray-500">{a.message}</p>
                      <p className="mt-1 text-xs text-gray-400">{a.appliedAt} 지원</p>
                    </div>
                  </div>
                  {a.status === "대기" && (
                    <div className="flex shrink-0 gap-2">
                      <button
                        onClick={() => updateStatus(a.id, "거절")}
                        className="flex items-center gap-1 rounded-lg border border-gray-200 px-3 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-50"
                      >
                        <X size={14} /> 거절
                      </button>
                      <button
                        onClick={() => updateStatus(a.id, "승인")}
                        className="flex items-center gap-1 rounded-lg bg-brand-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-700"
                      >
                        <Check size={14} /> 승인
                      </button>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </Card>
      </main>
    </>
  );
}
