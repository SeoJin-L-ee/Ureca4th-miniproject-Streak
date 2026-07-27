import { Navigate, Outlet } from "react-router-dom";
import TopNav from "../components/TopNav";
import { useAuth } from "../context/AuthContext";

export default function AppLayout() {
  const { user, loading } = useAuth();

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
    <div className="flex min-h-screen flex-col bg-[#f7f7fb]">
      <TopNav />
      <div className="flex flex-1 justify-center overflow-x-hidden px-6 py-6">
        <div className="flex w-full max-w-6xl flex-col overflow-hidden rounded-2xl border border-gray-100 bg-[#fbfbfd] shadow-sm">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
