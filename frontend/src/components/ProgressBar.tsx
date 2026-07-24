export default function ProgressBar({
  value,
  color = "#5b3fe0",
  height = 6,
}: {
  value: number;
  color?: string;
  height?: number;
}) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <div className="w-full rounded-full bg-gray-100" style={{ height }}>
      <div
        className="rounded-full transition-all"
        style={{ width: `${clamped}%`, height, backgroundColor: color }}
      />
    </div>
  );
}
