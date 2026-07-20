// Streak Security Test Console
// 목적: CSRF 쿠키 발급 -> 회원가입/로그인 -> 세션 쿠키 인증 -> 로그아웃까지
// 실제 백엔드(SecurityConfig)의 CORS/CSRF/세션 설정이 브라우저에서 그대로 동작하는지 눈으로 확인한다.

const API_BASE = "";

// ---------- 쿠키 유틸 ----------
function getCookie(name) {
  const match = document.cookie.match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
  return match ? decodeURIComponent(match[1]) : null;
}

function refreshCookieState() {
  const csrf = getCookie("XSRF-TOKEN");
  document.getElementById("csrfTokenVal").textContent = csrf || "(없음)";
  // JSESSIONID는 HttpOnly 쿠키라 document.cookie로 절대 읽히지 않는다. 그게 정상 동작이다.
  const jsession = getCookie("JSESSIONID");
  document.getElementById("jsessionVal").textContent = jsession
    ? jsession
    : "(JS로 접근 불가 — HttpOnly, 정상)";
}

// ---------- 로그 패널 ----------
const logListEl = document.getElementById("logList");

function renderLogEntry({ method, path, status, ok, ms, reqBody, resBody, note }) {
  const el = document.createElement("div");
  el.className = "log-entry " + (ok ? "ok" : "fail");

  const time = new Date().toLocaleTimeString("ko-KR", { hour12: false });

  el.innerHTML = `
    <div class="log-line1">
      <span class="log-method">${method}</span>
      <span class="log-path">${path}</span>
      <span class="log-status ${ok ? "ok" : "fail"}">${status}</span>
    </div>
    <div class="log-time">${time}${ms != null ? ` · ${ms}ms` : ""}</div>
    ${note ? `<div class="log-note">${note}</div>` : ""}
    ${reqBody ? `<div class="log-body">▶ 요청 body\n${reqBody}</div>` : ""}
    <div class="log-body">◀ 응답\n${resBody}</div>
  `;
  logListEl.prepend(el);
}

document.getElementById("clearLogBtn").addEventListener("click", () => {
  logListEl.innerHTML = "";
});

// ---------- 공통 fetch 래퍼 ----------
async function apiFetch(method, path, body, { skipCsrf = false, note } = {}) {
  const headers = {};
  if (body) headers["Content-Type"] = "application/json";

  if (method !== "GET" && !skipCsrf) {
    const token = getCookie("XSRF-TOKEN");
    if (token) headers["X-XSRF-TOKEN"] = token;
  }

  const started = performance.now();
  let res;
  let text;
  try {
    res = await fetch(API_BASE + path, {
      method,
      headers,
      credentials: "include", // 세션(JSESSIONID)/CSRF(XSRF-TOKEN) 쿠키를 크로스 오리진으로 주고받기 위해 필수
      body: body ? JSON.stringify(body) : undefined,
    });
    text = await res.text();
  } catch (networkErr) {
    setConn(false);
    renderLogEntry({
      method,
      path,
      status: "NETWORK ERROR",
      ok: false,
      reqBody: body ? JSON.stringify(body, null, 2) : null,
      resBody: String(networkErr),
      note: note || "백엔드에 연결할 수 없습니다. 스프링 앱이 8080에서 실행 중인지 확인하세요.",
    });
    throw networkErr;
  }
  setConn(true);

  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  const ms = Math.round(performance.now() - started);
  renderLogEntry({
    method,
    path,
    status: res.status,
    ok: res.ok,
    ms,
    reqBody: body ? JSON.stringify(body, null, 2) : null,
    resBody: JSON.stringify(data, null, 2),
    note,
  });

  refreshCookieState();
  return { ok: res.ok, status: res.status, data };
}

function setConn(ok) {
  const dot = document.getElementById("connDot");
  dot.classList.remove("ok", "fail");
  dot.classList.add(ok ? "ok" : "fail");
}

// ---------- 세션 UI 전환 ----------
function showLoggedIn(member) {
  document.getElementById("sessionLoggedOut").classList.add("hidden");
  document.getElementById("sessionLoggedIn").classList.remove("hidden");
  document.getElementById("avatarInitial").textContent = (member.name || "?").charAt(0);
  document.getElementById("profileName").textContent = member.name;
  document.getElementById("profileEmail").textContent = member.email;
  document.getElementById("profileId").textContent = member.memberId;
  document.getElementById("profilePhone").textContent = member.phone || "-";
}

function showLoggedOut() {
  document.getElementById("sessionLoggedOut").classList.remove("hidden");
  document.getElementById("sessionLoggedIn").classList.add("hidden");
}

// ---------- 탭 전환 ----------
document.querySelectorAll(".tab").forEach((tabBtn) => {
  tabBtn.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".panel").forEach((p) => p.classList.remove("active"));
    tabBtn.classList.add("active");
    document.getElementById(tabBtn.dataset.tab + "Form").classList.add("active");
  });
});

// ---------- 폼 핸들러 ----------
document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const body = { email: fd.get("email"), password: fd.get("password") };
  const { ok, data } = await apiFetch("POST", "/api/auth/login", body);
  if (ok) showLoggedIn(data.result);
});

document.getElementById("signupForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const body = {
    email: fd.get("email"),
    password: fd.get("password"),
    name: fd.get("name"),
    phone: fd.get("phone") || null,
  };
  const { ok } = await apiFetch("POST", "/api/auth/signup", body, {
    note: "가입 성공 후 자동 로그인은 되지 않습니다. 로그인 탭에서 로그인하세요.",
  });
  if (ok) {
    document.querySelector('.tab[data-tab="login"]').click();
    e.target.reset();
  }
});

document.getElementById("refreshMeBtn").addEventListener("click", async () => {
  const { ok, data, status } = await apiFetch("GET", "/api/auth/me");
  if (ok) showLoggedIn(data.result);
  else if (status === 401) showLoggedOut();
});

document.getElementById("logoutBtn").addEventListener("click", async () => {
  await apiFetch("POST", "/api/auth/logout");
  showLoggedOut();
});

document.getElementById("refreshCsrfBtn").addEventListener("click", () => {
  apiFetch("GET", "/api/auth/csrf");
});

// ---------- 보안 동작 테스트 버튼 ----------
document.getElementById("csrfBypassTestBtn").addEventListener("click", async () => {
  const dummy = {
    email: `csrf-test-${Date.now()}@example.com`,
    password: "test1234",
    name: "CSRF 테스트",
    phone: null,
  };
  await apiFetch("POST", "/api/auth/signup", dummy, {
    skipCsrf: true,
    note: "CSRF 헤더 없이 보낸 요청입니다. 403이 나와야 CSRF 보호가 정상 동작하는 것입니다.",
  });
});

document.getElementById("wrongPasswordTestBtn").addEventListener("click", async () => {
  await apiFetch(
    "POST",
    "/api/auth/login",
    { email: "nonexistent-or-wrong@example.com", password: "wrongpass1" },
    { note: "존재하지 않거나 틀린 비밀번호 — 401이 나와야 정상입니다." }
  );
});

// ---------- 초기 부트스트랩 ----------
(async function init() {
  await apiFetch("GET", "/api/auth/csrf", null, { note: "페이지 로드 시 CSRF 토큰을 미리 발급받습니다." });
  const { ok, data } = await apiFetch("GET", "/api/auth/me", null, { note: "기존 세션이 있는지 확인합니다." });
  if (ok) showLoggedIn(data.result);
  else showLoggedOut();
})();
