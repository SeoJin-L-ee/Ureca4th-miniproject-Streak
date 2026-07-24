// 공통 fetch 래퍼: 세션 쿠키 + CSRF 헤더를 자동으로 붙여준다.

interface CustomResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

export class ApiError extends Error {
  code: string;
  status: number;
  fieldErrors?: Record<string, string>;

  constructor(status: number, code: string, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

// 앱 시작 시 한 번 호출해서 XSRF-TOKEN 쿠키를 발급받는다.
export async function bootstrapCsrf(): Promise<void> {
  await fetch("/api/auth/csrf", { credentials: "include" });
}

const MUTATING_METHODS = new Set(["POST", "PATCH", "PUT", "DELETE"]);

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (MUTATING_METHODS.has(method)) {
    const csrfToken = getCookie("XSRF-TOKEN");
    if (csrfToken) {
      headers["X-XSRF-TOKEN"] = csrfToken;
    }
  }

  const res = await fetch(path, {
    method,
    credentials: "include",
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // 204 No Content (로그아웃 등)
  if (res.status === 204) {
    return undefined as T;
  }

  const json = (await res.json().catch(() => null)) as CustomResponse<T> | null;

  if (!res.ok || !json || !json.isSuccess) {
    const fieldErrors =
      json && json.result && typeof json.result === "object" && !Array.isArray(json.result)
        ? (json.result as unknown as Record<string, string>)
        : undefined;
    throw new ApiError(res.status, json?.code ?? String(res.status), json?.message ?? "요청에 실패했습니다.", fieldErrors);
  }

  return json.result;
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body ?? {}),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, body ?? {}),
  delete: <T>(path: string) => request<T>("DELETE", path),
};
