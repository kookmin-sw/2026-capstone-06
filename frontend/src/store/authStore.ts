import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  memberId: string | null;
  role: string | null;

  setTokens: (accessToken: string, refreshToken: string) => void;
  setMemberId: (memberId: string) => void;
  setRole: (role: string) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      memberId: null,
      role: null,

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken }),

      setMemberId: (memberId) => set({ memberId }),

      setRole: (role) => set({ role }),

      logout: () =>
        set({ accessToken: null, refreshToken: null, memberId: null, role: null }),

      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: 'auth-storage', // localStorage key
    }
  )
);
