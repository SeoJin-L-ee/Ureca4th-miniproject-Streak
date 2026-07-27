import { Link, Navigate, useNavigate } from "react-router-dom";
import { Users, Search, CalendarCheck } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import Avatar from "../components/Avatar";
import heroImg from "../assets/home-hero.png";

export default function Home() {
  const { user, loading } = useAuth();
  const navigate = useNavigate();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#f7f7fb] text-sm text-gray-400">
        불러오는 중...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="relative flex min-h-screen w-full items-center overflow-hidden">
      <img src={heroImg} alt="" aria-hidden="true" className="absolute inset-0 h-full w-full object-cover" />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-y-0 left-0 w-full bg-gradient-to-r from-brand-50 from-10% via-brand-50/70 via-40% to-transparent sm:w-4/5 lg:w-3/4"
      />

      <Link
        to="/members/me"
        className="absolute right-6 top-6 z-10 flex items-center gap-2 sm:right-10 sm:top-8"
      >
        <Avatar name={user.name} size={32} />
        <span className="text-sm font-medium text-gray-700 sm:text-base">{user.name}</span>
      </Link>

      <div className="relative z-10 w-full px-8 py-14 sm:px-28 sm:py-20 lg:px-48 xl:px-56">
        <div className="max-w-xl">
          <h1 className="text-4xl font-extrabold leading-tight text-gray-900 sm:text-5xl">
            <span className="text-brand-600">꾸준한 습관</span>이 모여
            <br />
            <span className="text-brand-600">실력</span>이 됩니다
          </h1>

          <p className="mt-6 flex items-center gap-1.5 text-lg font-medium text-gray-500 sm:text-xl">
            <span className="font-bold text-brand-600">Streak</span>
            <span>· 스터디 관리 서비스</span>
          </p>

          <div className="mt-9 flex flex-wrap gap-3">
            <button
              onClick={() => navigate("/studies")}
              className="flex items-center gap-2 rounded-xl bg-brand-600 px-5 py-3.5 text-base font-semibold text-white shadow-sm transition-colors hover:bg-brand-700"
            >
              <Users size={19} />내 스터디 보기
            </button>
            <button
              onClick={() => navigate("/explore")}
              className="flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-5 py-3.5 text-base font-semibold text-gray-700 shadow-sm transition-colors hover:bg-gray-50"
            >
              <Search size={19} />
              스터디 찾기
            </button>
            <button
              onClick={() => navigate("/members/me/calendar")}
              className="flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-5 py-3.5 text-base font-semibold text-gray-700 shadow-sm transition-colors hover:bg-gray-50"
            >
              <CalendarCheck size={19} />
              오늘 일정 확인
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
