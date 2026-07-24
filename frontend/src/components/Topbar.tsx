import { Bell } from "lucide-react";
import Avatar from "./Avatar";
import { useAuth } from "../context/AuthContext";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

export default function Topbar({ title, breadcrumb }: { title?: string; breadcrumb?: ReactNode }) {
  const { user } = useAuth();

  return (
    <header className="flex h-16 shrink-0 items-center justify-between border-b border-gray-200 bg-white/80 px-6 backdrop-blur">
      <div className="text-sm text-gray-500">
        {breadcrumb}
        {title && <span className="text-base font-semibold text-gray-900">{title}</span>}
      </div>
      <div className="flex items-center gap-4">
        <button className="relative flex h-9 w-9 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-600">
          <Bell size={18} />
          <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-red-500" />
        </button>
        <Link to="/members/me" className="flex items-center gap-2">
          <Avatar name={user?.name ?? "?"} size={32} />
          <span className="text-sm font-medium text-gray-700">{user?.name}</span>
        </Link>
      </div>
    </header>
  );
}
