const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

export function formatDate(iso: string) {
  const d = new Date(iso);
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${mm}.${dd}(${WEEKDAYS[d.getDay()]})`;
}

export function formatDateTime(iso: string) {
  const d = new Date(iso);
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${mm}.${dd}(${WEEKDAYS[d.getDay()]}) ${hh}:${min}`;
}

// D-day 뱃지 텍스트: 마감 지난 경우 "D--3"처럼 대시가 두 번 나오지 않도록 처리
export function formatDday(daysLeft: number) {
  return daysLeft < 0 ? "마감" : `D-${daysLeft}`;
}

// 전체 회차 테이블 등에서 쓰는 "yyyy.mm.dd" 형식
export function formatYmd(iso: string) {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}.${mm}.${dd}`;
}

export function formatTime(iso: string) {
  const d = new Date(iso);
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm}`;
}

export function daysUntil(iso: string) {
  const now = new Date();
  const target = new Date(iso);
  const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  return Math.ceil((startOfDay(target) - startOfDay(now)) / (1000 * 60 * 60 * 24));
}

export function isToday(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate();
}

export function toLocalDateStr(date: Date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}
