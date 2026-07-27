import { useEffect, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { Check, X } from "lucide-react";
import Topbar from "../components/Topbar";
import Card from "../components/Card";
import Badge from "../components/Badge";
import Avatar from "../components/Avatar";
import * as studiesApi from "../api/studies";
import * as applicationsApi from "../api/applications";
import { ApiError } from "../api/client";
import type { ApplicationResDto, ApplicationStatus } from "../api/types";
import { applicationStatusLabel, applicationStatusTone } from "../lib/labels";
import { formatDateTime } from "../lib/format";

const tabs: ApplicationStatus[] = ["PENDING", "APPROVED", "REJECTED"];

export default function ApplicantManage() {
  const { studyId } = useParams<{ studyId: string }>();
  const id = Number(studyId);
  const [studyTitle, setStudyTitle] = useState<string>("");
  const [apps, setApps] = useState<ApplicationResDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<ApplicationStatus>("PENDING");
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [updateError, setUpdateError] = useState<string | null>(null);

  const load = () => {
    setLoading(true);
    Promise.all([
      studiesApi.getStudyDashboard(id).then((res) => setStudyTitle(res.title)),
      applicationsApi.getApplications(id).then(setApps),
    ])
      .catch(() => setError("지원자 목록을 불러오지 못했어요."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (Number.isFinite(id)) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!studyId || !Number.isFinite(id)) return <Navigate to="/studies" replace />;

  const updateStatus = async (applicationId: number, status: ApplicationStatus) => {
    setUpdatingId(applicationId);
    setUpdateError(null);
    try {
      await applicationsApi.updateApplicationStatus(applicationId, status);
      load();
    } catch (e) {
      setUpdateError(e instanceof ApiError ? e.message : "처리에 실패했어요. 잠시 후 다시 시도해주세요.");
    } finally {
      setUpdatingId(null);
    }
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
        <Card>
          {loading && <p className="py-8 text-center text-sm text-gray-400">불러오는 중...</p>}
          {error && <p className="py-8 text-center text-sm text-red-500">{error}</p>}
          {!loading && !error && (
            <>
              {updateError && (
                <p className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{updateError}</p>
              )}
              <div className="mb-4 flex gap-1 rounded-xl bg-gray-50 p-1">
                {tabs.map((t) => (
                  <button
                    key={t}
                    onClick={() => setTab(t)}
                    className={`flex-1 rounded-lg py-1.5 text-sm font-medium transition-colors ${
                      tab === t ? "bg-white text-brand-700 shadow-sm" : "text-gray-500 hover:text-gray-700"
                    }`}
                  >
                    {applicationStatusLabel[t]} ({apps.filter((a) => a.status === t).length})
                  </button>
                ))}
              </div>

              <ul className="space-y-3">
                {filtered.length === 0 && <p className="py-8 text-center text-sm text-gray-400">해당 상태의 지원자가 없어요.</p>}
                {filtered.map((a) => (
                  <li key={a.applicationId} className="rounded-xl border border-gray-100 p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3">
                        <Avatar name={a.applicantName} size={40} />
                        <div>
                          <div className="mb-1 flex items-center gap-2">
                            <p className="text-sm font-semibold text-gray-800">{a.applicantName}</p>
                            <Badge tone={applicationStatusTone[a.status]}>{applicationStatusLabel[a.status]}</Badge>
                          </div>
                          <p className="text-sm text-gray-500">{a.content}</p>
                          <p className="mt-1 text-xs text-gray-400">{formatDateTime(a.appliedAt)} 지원</p>
                        </div>
                      </div>
                      {a.status === "PENDING" && (
                        <div className="flex shrink-0 gap-2">
                          <button
                            onClick={() => updateStatus(a.applicationId, "REJECTED")}
                            disabled={updatingId === a.applicationId}
                            className="flex items-center gap-1 rounded-lg border border-gray-200 px-3 py-1.5 text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-60"
                          >
                            <X size={14} /> 거절
                          </button>
                          <button
                            onClick={() => updateStatus(a.applicationId, "APPROVED")}
                            disabled={updatingId === a.applicationId}
                            className="flex items-center gap-1 rounded-lg bg-brand-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-700 disabled:opacity-60"
                          >
                            <Check size={14} /> 승인
                          </button>
                        </div>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </>
          )}
        </Card>
      </main>
    </>
  );
}
