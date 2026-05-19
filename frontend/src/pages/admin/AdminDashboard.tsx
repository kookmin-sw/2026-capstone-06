import { useState, useRef, useCallback, useEffect } from "react";
import { useNavigate } from "react-router";
import { ShieldCheck, Users, Hash, Cpu, LayoutDashboard, LogOut, Wifi, PanelLeft } from "lucide-react";
import { useAuthStore } from "../../store/authStore";
import { Badge } from "../../components/ui/badge";
import { Card, CardContent } from "../../components/ui/card";
import { MemberManager } from "./MemberManager";
import { SerialManager } from "./SerialManager";
import { DeviceManager } from "./DeviceManager";
import { AdminOverview } from "./AdminOverview";
import { type Serial } from "./mockData";
import { getSerials } from "../../services/serialApi";
import { getMembers, type MemberDto } from "../../services/memberApi";
import { getDevices, type DeviceDto } from "../../services/deviceApi";

type TabKey = "overview" | "members" | "serials" | "devices";

const tabs: { key: TabKey; label: string; icon: React.ComponentType<{ className?: string }> }[] = [
  { key: "overview", label: "대시보드", icon: LayoutDashboard },
  { key: "members", label: "회원 관리", icon: Users },
  { key: "serials", label: "시리얼 관리", icon: Hash },
  { key: "devices", label: "장치 관리", icon: Cpu },
];

const SIDEBAR_MIN = 180;
const SIDEBAR_MAX = 420;
const SIDEBAR_DEFAULT = 256;

export function AdminDashboard() {
  const [tab, setTab] = useState<TabKey>("overview");
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [sidebarWidth, setSidebarWidth] = useState(SIDEBAR_DEFAULT);
  const [members, setMembers] = useState<MemberDto[]>([]);
  const [serials, setSerials] = useState<Serial[]>([]);
  const [devices, setDevices] = useState<DeviceDto[]>([]);
  const navigate = useNavigate();
  const { memberId, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate("/auth");
  };

  const loadMembers = useCallback(async () => {
    try {
      const data = await getMembers();
      setMembers(data.content);
    } catch (error) {
      console.error("Failed to load members:", error);
    }
  }, []);

  const loadSerials = useCallback(async () => {
    try {
      const data = await getSerials();
      setSerials(data.content);
    } catch (error) {
      console.error("Failed to load serials:", error);
    }
  }, []);

  const loadDevices = useCallback(async () => {
    try {
      const data = await getDevices();
      setDevices(data.content);
    } catch (error) {
      console.error("Failed to load devices:", error);
    }
  }, []);

  useEffect(() => {
    loadMembers();
    loadSerials();
    loadDevices();
  }, [loadMembers, loadSerials, loadDevices]);

  const dragging = useRef(false);
  const startX = useRef(0);
  const startWidth = useRef(SIDEBAR_DEFAULT);

  const onMouseDown = useCallback((e: React.MouseEvent) => {
    dragging.current = true;
    startX.current = e.clientX;
    startWidth.current = sidebarWidth;
    document.body.style.cursor = "col-resize";
    document.body.style.userSelect = "none";
  }, [sidebarWidth]);

  useEffect(() => {
    const onMouseMove = (e: MouseEvent) => {
      if (!dragging.current) return;
      const delta = e.clientX - startX.current;
      const newWidth = startWidth.current + delta;
      if (newWidth < SIDEBAR_MIN - 40) {
        setSidebarOpen(false);
      } else {
        setSidebarOpen(true);
        setSidebarWidth(Math.min(SIDEBAR_MAX, Math.max(SIDEBAR_MIN, newWidth)));
      }
    };
    const onMouseUp = () => {
      if (dragging.current) {
        dragging.current = false;
        document.body.style.cursor = "";
        document.body.style.userSelect = "";
      }
    };
    window.addEventListener("mousemove", onMouseMove);
    window.addEventListener("mouseup", onMouseUp);
    return () => {
      window.removeEventListener("mousemove", onMouseMove);
      window.removeEventListener("mouseup", onMouseUp);
    };
  }, []);

  const handleToggle = () => {
    if (!sidebarOpen) {
      setSidebarOpen(true);
      setSidebarWidth(SIDEBAR_DEFAULT);
    } else {
      setSidebarOpen(false);
    }
  };

  const breadcrumb =
    tab === "overview" ? "대시보드"
    : tab === "members" ? "회원 관리"
    : tab === "serials" ? "시리얼 관리"
    : "장치 관리";

  return (
    <div className="size-full min-h-screen bg-slate-50 flex flex-col overflow-hidden">
      {/* Top Banner - Full Width */}
      <header className="bg-gradient-to-r from-violet-600 to-indigo-600 text-white flex-shrink-0 shadow-sm z-20">
        <div className="px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={handleToggle}
              title={sidebarOpen ? "사이드바 숨기기" : "사이드바 열기"}
              className="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-white/20 transition-colors text-white flex-shrink-0"
            >
              <PanelLeft className="w-5 h-5" />
            </button>
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-white/20 flex items-center justify-center">
                <ShieldCheck className="w-5 h-5 text-white" />
              </div>
              <div className="flex items-baseline gap-2">
                <h1 className="text-lg font-bold tracking-tight">펫하우스 관리 시스템</h1>
                <span className="text-white/80 text-sm hidden sm:inline-block">(반려동물 건강 모니터링)</span>
              </div>
            </div>
          </div>
          
          <div className="flex items-center gap-4">
            <Badge className="bg-emerald-500/20 text-emerald-50 hover:bg-emerald-500/30 border-emerald-500/30">
              <Wifi className="w-3 h-3 mr-1" /> 연결됨
            </Badge>
          </div>
        </div>
      </header>

      {/* Body Container */}
      <div className="flex flex-1 overflow-hidden relative">
        {/* Left Sidebar */}
        <aside
          className="bg-white border-r flex flex-col overflow-hidden relative flex-shrink-0 z-10"
          style={{
            width: sidebarOpen ? sidebarWidth : 0,
            transition: dragging.current ? "none" : "width 0.25s ease",
            borderRightWidth: sidebarOpen ? 1 : 0,
          }}
        >
          {/* Stats */}
          <div className="p-4 space-y-2 border-b">
            <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">시스템 현황</div>
            <div className="rounded-lg border bg-slate-50/50 p-3 space-y-2.5">
              <SidebarStat label="회원" value={members.length} />
              <SidebarStat label="시리얼" value={`${serials.filter(s => s.isUse).length} / ${serials.length}`} />
              <SidebarStat label="장치" value={devices.length} />
            </div>
          </div>

          {/* Nav Menu */}
          <nav className="p-3 space-y-1 flex-1 overflow-y-auto">
            <div className="px-3 py-2 text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">메뉴</div>
            {tabs.map(({ key, label, icon: Icon }) => (
              <button
                key={key}
                onClick={() => setTab(key)}
                className={`w-full text-left px-3 py-2.5 rounded-lg flex items-center gap-3 whitespace-nowrap transition-colors ${
                  tab === key ? "bg-violet-100 text-violet-700 font-medium" : "hover:bg-slate-100 text-slate-600"
                }`}
              >
                <Icon className={`w-5 h-5 flex-shrink-0 ${tab === key ? "text-violet-600" : "text-slate-400"}`} />
                {label}
              </button>
            ))}
          </nav>

          {/* User footer */}
          <div className="p-4 border-t flex items-center justify-between min-w-0 bg-slate-50/50 mt-auto">
            <div className="flex items-center gap-3 min-w-0">
              <div className="w-9 h-9 rounded-full bg-violet-100 text-violet-600 flex items-center justify-center flex-shrink-0">
                <span className="text-lg leading-none mb-1">👤</span>
              </div>
              <div className="min-w-0 truncate">
                <div className="text-sm font-semibold text-slate-700 truncate">{memberId ?? "관리자"}</div>
                <div className="text-xs text-muted-foreground truncate">ADMIN</div>
              </div>
            </div>
            <button 
              onClick={handleLogout}
              className="p-2 rounded-lg hover:bg-slate-200 text-slate-500 hover:text-slate-700 transition-colors flex-shrink-0" 
              title="로그아웃"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>

          {/* Drag handle */}
          <div
            onMouseDown={onMouseDown}
            className="absolute top-0 right-0 h-full w-1.5 cursor-col-resize hover:bg-violet-300 active:bg-violet-500 transition-colors group z-20"
            title="드래그하여 사이드바 크기 조정"
          >
            <div className="absolute top-1/2 right-[1px] -translate-y-1/2 w-1 h-12 rounded-full bg-slate-200 group-hover:bg-violet-400 transition-colors" />
          </div>
        </aside>

        {/* Right Main Content */}
        <main className="flex-1 flex flex-col min-w-0 bg-slate-50 overflow-hidden">
          {/* Right Navigation Bar */}
          <div className="bg-white border-b px-6 flex items-center gap-6 flex-shrink-0 overflow-x-auto z-10 sticky top-0 shadow-sm">
            {tabs.map(({ key, label, icon: Icon }) => (
              <button
                key={key}
                onClick={() => setTab(key)}
                className={`flex items-center gap-2 py-4 border-b-2 transition-colors whitespace-nowrap flex-shrink-0 outline-none ${
                  tab === key
                    ? "border-violet-600 text-violet-700 font-semibold"
                    : "border-transparent text-muted-foreground hover:text-slate-900 hover:border-slate-300"
                }`}
              >
                <Icon className="w-4 h-4" />
                {label}
              </button>
            ))}
          </div>

          {/* Content Area */}
          <div className="flex-1 overflow-auto p-6">
            <div className="max-w-6xl mx-auto space-y-6 pb-10">
              <div className="flex items-center gap-2 text-muted-foreground text-sm">
                <span>관리자 콘솔</span>
                <span>/</span>
                <span className="font-medium text-slate-700">{breadcrumb}</span>
              </div>

              {tab === "overview" && <AdminOverview members={members} serials={serials} devices={devices} onNavigate={setTab} />}
              {tab === "members" && (
                <Card className="shadow-sm border-slate-200"><CardContent className="p-0 sm:p-6"><MemberManager members={members} setMembers={setMembers} onReload={loadMembers} /></CardContent></Card>
              )}
              {tab === "serials" && (
                <Card className="shadow-sm border-slate-200"><CardContent className="p-0 sm:p-6"><SerialManager serials={serials} setSerials={setSerials} onReload={loadSerials} /></CardContent></Card>
              )}
              {tab === "devices" && (
                <Card className="shadow-sm border-slate-200"><CardContent className="p-0 sm:p-6"><DeviceManager devices={devices} setDevices={setDevices} members={members} serials={serials} setSerials={setSerials} onReload={loadDevices} /></CardContent></Card>
              )}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

function SidebarStat({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-violet-700">{value}</span>
    </div>
  );
}