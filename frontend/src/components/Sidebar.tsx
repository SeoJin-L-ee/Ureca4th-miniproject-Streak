import { NavLink } from "react-router-dom";
import { Compass, LayoutGrid, CalendarDays, User, Zap } from "lucide-react";

const navItems = [
  { to: "/studies", label: "내 스터디", icon: LayoutGrid, end: false },
  { to: "/explore", label: "스터디 찾기", icon: Compass, end: false },
  { to: "/members/me/calendar", label: "캘린더", icon: CalendarDays, end: false },
  { to: "/members/me", label: "마이페이지", icon: User, end: true },
];

export default function Sidebar() {
  return (
    <aside className="sticky top-0 flex h-screen w-56 shrink-0 flex-col border-r border-gray-200 bg-white">
      <div className="flex items-center gap-2 px-5 py-5">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-white">
          <Zap size={18} fill="white" />
        </div>
        <span className="text-lg font-bold text-gray-900">Streak</span>
      </div>
      <nav className="flex flex-1 flex-col gap-1 px-3">
        {navItems.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive ? "bg-brand-50 text-brand-700" : "text-gray-500 hover:bg-gray-50 hover:text-gray-800"
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-gray-100 px-3 py-4 text-xs text-gray-400">© 2026 Streak. All rights reserved.</div>
    </aside>
  );
}
