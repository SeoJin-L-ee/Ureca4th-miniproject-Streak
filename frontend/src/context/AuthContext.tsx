import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { bootstrapCsrf } from "../api/client";
import * as authApi from "../api/auth";
import * as memberApi from "../api/member";
import type { MemberResDto } from "../api/types";

interface AuthContextValue {
  user: MemberResDto | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, name: string, phone: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MemberResDto | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = async () => {
    try {
      const me = await memberApi.getMe();
      setUser(me);
    } catch {
      setUser(null);
    }
  };

  useEffect(() => {
    (async () => {
      await bootstrapCsrf();
      await refreshMe();
      setLoading(false);
    })();
  }, []);

  const login = async (email: string, password: string) => {
    const auth = await authApi.login(email, password);
    setUser({ memberId: auth.memberId, email: auth.email, name: auth.name, phone: auth.phone });
  };

  const signup = async (email: string, password: string, name: string, phone: string) => {
    await authApi.signup(email, password, name, phone);
    await login(email, password);
  };

  const logout = async () => {
    await authApi.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, signup, logout, refreshMe }}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
