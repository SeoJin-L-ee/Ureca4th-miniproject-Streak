import type { ReactNode } from "react";

export default function Topbar({ title, breadcrumb }: { title?: string; breadcrumb?: ReactNode }) {
  return (
    <div className="flex h-14 shrink-0 items-center px-6">
      <div className="text-sm text-gray-500">
        {breadcrumb}
        {title && <span className="text-base font-semibold text-gray-900">{title}</span>}
      </div>
    </div>
  );
}
