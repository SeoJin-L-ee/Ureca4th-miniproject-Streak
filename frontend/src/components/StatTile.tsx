import type { ReactNode } from "react";

export default function StatTile({
  icon,
  label,
  value,
  sub,
  iconBg = "bg-brand-50",
}: {
  icon: ReactNode;
  label: string;
  value: ReactNode;
  sub?: string;
  iconBg?: string;
}) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${iconBg}`}>{icon}</div>
      <div className="min-w-0">
        <p className="text-xs text-gray-500">{label}</p>
        <p className="truncate text-lg font-bold text-gray-900">{value}</p>
        {sub && <p className="text-xs text-gray-400">{sub}</p>}
      </div>
    </div>
  );
}
