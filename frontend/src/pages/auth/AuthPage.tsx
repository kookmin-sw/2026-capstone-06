import { useState } from "react";
import { useNavigate } from "react-router";
import { ShieldCheck, Mail, Lock, User, Phone } from "lucide-react";
import { toast } from "sonner";
import * as authApi from "../../services/authApi";
import { useAuthStore } from "../../store/authStore";
import { usePetStore } from "../../store/petStore";

export function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [memberId, setMemberId] = useState("");
  const [memberPw, setMemberPw] = useState("");
  const [memberName, setMemberName] = useState("");
  const [memberPhone, setMemberPhone] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const { setTokens, setMemberId: storeMemberId, setRole } = useAuthStore();
  const { loadDevicesFromServer } = usePetStore();

  const resetForm = () => {
    setMemberId("");
    setMemberPw("");
    setMemberName("");
    setMemberPhone("");
  };

  const handleLogin = async () => {
    // 1. 백엔드 로그인 API 호출 → accessToken, refreshToken 획득
    const loginRes = await authApi.loginWeb({ memberId, memberPw });

    // 2. 토큰 및 사용자 정보를 authStore에 저장 (localStorage 영속)
    setTokens(loginRes.accessToken, loginRes.refreshToken);
    storeMemberId(memberId);
    setRole(loginRes.role);

    // 3. 역할에 따라 라우팅
    if (loginRes.role === "ADMIN") {
      navigate("/admin");
      return;
    }

    // 4. 일반 유저: 기기(펫하우스) 목록을 백엔드에서 로드 → petStore에 저장
    await loadDevicesFromServer(memberId);
    toast.success("로그인 성공!");
    navigate("/user");
  };

  const handleRegister = async () => {
    if (!memberId || !memberPw || !memberName || !memberPhone) {
      toast.error("모든 항목을 입력해 주세요.");
      return;
    }
    await authApi.register({ memberId, memberPw, memberName, memberPhone });
    toast.success("회원가입이 완료되었습니다. 로그인해 주세요.");
    resetForm();
    setIsLogin(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      if (isLogin) {
        await handleLogin();
      } else {
        await handleRegister();
      }
    } catch (error: any) {
      console.error("[AuthPage] 처리 실패:", error);
      const msg =
        error?.response?.data?.message ??
        (isLogin
          ? "아이디 또는 비밀번호를 확인해 주세요."
          : "회원가입 중 오류가 발생했습니다.");
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const switchTab = (login: boolean) => {
    setIsLogin(login);
    resetForm();
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex flex-col items-center gap-3 mb-8">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-violet-600 to-indigo-600 flex items-center justify-center text-white shadow-lg">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <div className="text-center">
            <h1 className="text-2xl font-bold tracking-tight text-slate-900">펫하우스 관리 시스템</h1>
            <p className="text-slate-500 mt-1">
              {isLogin ? "계정에 로그인하세요" : "새 계정을 만드세요"}
            </p>
          </div>
        </div>

        {/* Card */}
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-100">
          <div className="mb-6">
            <div className="flex gap-4 border-b border-slate-100 pb-1">
              <button
                className={`pb-3 font-medium transition-colors border-b-2 -mb-[2px] ${
                  isLogin
                    ? "text-violet-600 border-violet-600"
                    : "text-slate-400 border-transparent hover:text-slate-600"
                }`}
                onClick={() => switchTab(true)}
              >
                로그인
              </button>
              <button
                className={`pb-3 font-medium transition-colors border-b-2 -mb-[2px] ${
                  !isLogin
                    ? "text-violet-600 border-violet-600"
                    : "text-slate-400 border-transparent hover:text-slate-600"
                }`}
                onClick={() => switchTab(false)}
              >
                회원가입
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* 이름 - 회원가입 전용 */}
            {!isLogin && (
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-slate-700">이름</label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type="text"
                    placeholder="홍길동"
                    value={memberName}
                    onChange={(e) => setMemberName(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                    required={!isLogin}
                  />
                </div>
              </div>
            )}

            {/* 아이디 */}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">아이디</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="text"
                  placeholder="아이디를 입력하세요"
                  value={memberId}
                  onChange={(e) => setMemberId(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                  required
                />
              </div>
            </div>

            {/* 비밀번호 */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-sm font-medium text-slate-700">비밀번호</label>
                {isLogin && (
                  <button type="button" className="text-xs text-violet-600 hover:text-violet-700 font-medium">
                    비밀번호 찾기
                  </button>
                )}
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="password"
                  placeholder="••••••••"
                  value={memberPw}
                  onChange={(e) => setMemberPw(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                  required
                />
              </div>
            </div>

            {/* 전화번호 - 회원가입 전용 */}
            {!isLogin && (
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-slate-700">전화번호</label>
                <div className="relative">
                  <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type="tel"
                    placeholder="010-1234-5678"
                    value={memberPhone}
                    onChange={(e) => setMemberPhone(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                    required={!isLogin}
                  />
                </div>
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white font-medium py-3 rounded-xl transition-colors mt-6"
            >
              {isLoading ? "처리 중..." : isLogin ? "로그인" : "가입하기"}
            </button>
          </form>
        </div>

        {/* Footer */}
        <p className="text-center text-sm text-slate-400 mt-8">
          © 2026 Pethouse. All rights reserved.
        </p>
      </div>
    </div>
  );
}