import { NavLink, Link } from "react-router-dom";
import { Zap } from "lucide-react";
import Avatar from "./Avatar";
import { useAuth } from "../context/AuthContext";

const navItems = [
  { to: "/studies", label: "내 스터디", end: false },
  { to: "/explore", label: "스터디 찾기", end: false },
  { to: "/members/me/calendar", label: "캘린더", end: false },
];

export default function TopNav() {
  const { user } = useAuth();

  return (
    <header className="relative flex h-14 shrink-0 items-center justify-between border-b border-gray-200 bg-white px-6">
      <div className="flex items-center gap-7">
        <Link to="/" className="flex items-center gap-2">
          <div className="flex h-6.5 w-6.5 items-center justify-center rounded-lg bg-brand-600 text-white">
            <Zap size={15} fill="white" />
          </div>
          <span className="text-[15px] font-bold text-gray-900">Streak</span>
        </Link>
        <nav className="flex items-center gap-5">
          {navItems.map(({ to, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `flex h-14 items-center border-b-2 text-[13px] font-semibold transition-colors ${
                  isActive ? "border-brand-600 text-brand-700" : "border-transparent text-gray-500 hover:text-gray-800"
                }`
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </div>

      <Link to="/members/me" className="flex items-center gap-2">
        <Avatar name={user?.name ?? "?"} size={28} />
        <span className="text-[13px] font-medium text-gray-700">{user?.name}</span>
      </Link>
    </header>
  );
}
