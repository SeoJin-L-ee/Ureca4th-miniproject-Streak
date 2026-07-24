import { api } from "./client";
import type { AuthResDto } from "./types";

export function login(email: string, password: string) {
  return api.post<AuthResDto>("/api/auth/login", { email, password });
}

export function signup(email: string, password: string, name: string, phone: string) {
  return api.post<AuthResDto>("/api/auth/signup", { email, password, name, phone });
}

export function logout() {
  return api.post<void>("/api/auth/logout").catch(() => undefined);
}
