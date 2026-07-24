import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis, Cell, LabelList } from "recharts";

export interface BarDatum {
  label: string;
  value: number;
}

export default function MiniBarChart({
  data,
  colorLight = "#bfdbfe",
  colorDark = "#3b82f6",
  height = 160,
}: {
  data: BarDatum[];
  colorLight?: string;
  colorDark?: string;
  height?: number;
}) {
  return (
    <ResponsiveContainer width="100%" height={height}>
      <BarChart data={data} margin={{ top: 20, right: 8, left: -20, bottom: 0 }}>
        <XAxis dataKey="label" tick={{ fontSize: 12, fill: "#6b7280" }} axisLine={false} tickLine={false} />
        <YAxis
          domain={[0, 100]}
          ticks={[0, 50, 100]}
          tickFormatter={(v) => `${v}%`}
          tick={{ fontSize: 11, fill: "#9ca3af" }}
          axisLine={false}
          tickLine={false}
          width={40}
        />
        <Bar dataKey="value" radius={[6, 6, 0, 0]} barSize={56}>
          {data.map((_, i) => (
            <Cell key={i} fill={i === data.length - 1 ? colorDark : colorLight} />
          ))}
          <LabelList
            dataKey="value"
            position="top"
            formatter={(v: unknown) => `${v}%`}
            style={{ fontWeight: 700, fontSize: 13, fill: "#1f2028" }}
          />
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
