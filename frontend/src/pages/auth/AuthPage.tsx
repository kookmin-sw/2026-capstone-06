import { useState } from "react";
import { useNavigate } from "react-router";
import { ShieldCheck, Mail, Lock, User } from "lucide-react";

export function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // 이메일이 admin@pethouse.com 이면 관리자 페이지로, 아니면 유저 페이지로 이동
    if (email === "admin@pethouse.com") {
      navigate("/admin");
    } else {
      navigate("/user");
    }
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
            <p className="text-slate-500 mt-1">관리자 콘솔에 접속하려면 로그인하세요</p>
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
                onClick={() => setIsLogin(true)}
              >
                로그인
              </button>
              <button
                className={`pb-3 font-medium transition-colors border-b-2 -mb-[2px] ${
                  !isLogin
                    ? "text-violet-600 border-violet-600"
                    : "text-slate-400 border-transparent hover:text-slate-600"
                }`}
                onClick={() => setIsLogin(false)}
              >
                회원가입
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-slate-700">이름</label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type="text"
                    placeholder="홍길동"
                    className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                    required={!isLogin}
                  />
                </div>
              </div>
            )}
            
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700">이메일</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="email"
                  placeholder="admin@pethouse.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                  required
                />
              </div>
            </div>

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
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600/20 focus:border-violet-600 transition-all"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-violet-600 hover:bg-violet-700 text-white font-medium py-3 rounded-xl transition-colors mt-6"
            >
              {isLogin ? "로그인" : "가입하기"}
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