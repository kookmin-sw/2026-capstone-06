import { useState, useRef, useCallback, useEffect } from "react";
import { Outlet, Link, useLocation } from "react-router";
import {
  Home,
  BarChart3,
  Utensils,
  Wind,
  Dog,
  Plus,
  X,
  Check,
  Menu,
  Wifi,
  WifiOff,
  ChevronRight,
  Trash2,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
  Stethoscope,
} from "lucide-react";
import { usePetHouse, type PetHouse, COLOR_MAP, PET_EMOJI } from "../store/petStore";
import { useAuthStore } from "../store/authStore";
import { toast } from "sonner";
import { checkSerial, createDevice } from "../services/deviceApi";
import { Sparkles, Key, Calendar, AlertCircle, Loader2, LogOut } from "lucide-react";

const PET_TYPE_LABEL: Record<string, string> = {
  dog: "강아지",
  cat: "고양이",
  other: "기타",
};

function hasPetName(petName: string) {
  const trimmed = petName?.trim();
  return Boolean(trimmed && trimmed !== "-");
}

const COLOR_OPTIONS = ["blue", "purple", "green", "orange", "pink", "teal", "indigo", "rose"];

function AddHouseModal({ onClose }: { onClose: () => void }) {
  const { addHouse } = usePetHouse();
  const [form, setForm] = useState({
    name: "",
    petName: "",
    petType: "dog" as PetHouse["petType"],
    location: "",
    online: true,
    color: "blue",
  });

  const handleSubmit = () => {
    if (!form.name.trim() || !form.petName.trim()) {
      toast.error("하우스 이름과 반려동물 이름을 입력해주세요");
      return;
    }
    addHouse(form);
    toast.success(`${form.name}이(가) 추가되었습니다`);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold text-gray-900">새 펫하우스 추가</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors">
            <X className="w-4 h-4 text-gray-500" />
          </button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium text-gray-700 block mb-1.5">하우스 이름</label>
            <input
              type="text"
              placeholder="예: 4번 하우스"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 bg-gray-50"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 block mb-1.5">반려동물 이름</label>
            <input
              type="text"
              placeholder="예: 보리"
              value={form.petName}
              onChange={(e) => setForm({ ...form, petName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 bg-gray-50"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1.5">반려동물 종류</label>
              <select
                value={form.petType}
                onChange={(e) => setForm({ ...form, petType: e.target.value as PetHouse["petType"] })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 bg-gray-50"
              >
                <option value="dog">🐶 강아지</option>
                <option value="cat">🐱 고양이</option>
                <option value="other">🐾 기타</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1.5">위치</label>
              <input
                type="text"
                placeholder="예: 거실"
                value={form.location}
                onChange={(e) => setForm({ ...form, location: e.target.value })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 bg-gray-50"
              />
            </div>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700 block mb-2">테마 색상</label>
            <div className="flex gap-2 flex-wrap">
              {COLOR_OPTIONS.map((c) => (
                <button
                  key={c}
                  onClick={() => setForm({ ...form, color: c })}
                  className={`w-8 h-8 rounded-full ${COLOR_MAP[c].bg} flex items-center justify-center transition-all ${
                    form.color === c ? "ring-2 ring-offset-2 ring-gray-500 scale-110" : "hover:scale-105 opacity-70 hover:opacity-100"
                  }`}
                >
                  {form.color === c && <Check className="w-4 h-4 text-white" />}
                </button>
              ))}
            </div>
          </div>
        </div>
        <div className="flex gap-2 mt-6">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            className="flex-1 px-4 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-xl text-sm font-medium transition-colors"
          >
            추가하기
          </button>
        </div>
      </div>
    </div>
  );
}

const NAV_ITEMS = [
  { path: "/user", label: "대시보드", icon: Home },
  { path: "/user/feed-water", label: "급여/급수", icon: Utensils },
  { path: "/user/ventilation", label: "환풍기", icon: Wind },
  { path: "/user/statistics", label: "통계", icon: BarChart3 },
  { path: "/user/hospital", label: "동물병원", icon: Stethoscope },
  { path: "/user/settings", label: "설정", icon: Settings },
];

const PAGE_TITLES: Record<string, string> = {
  "/user": "대시보드",
  "/user/feed-water": "급여 / 급수",
  "/user/ventilation": "환풍기 제어",
  "/user/statistics": "통계",
  "/user/hospital": "동물병원",
  "/user/settings": "설정",
};

function DeviceRegistrationScreen() {
  const { memberId, logout } = useAuthStore();
  const { loadDevicesFromServer } = usePetHouse();
  
  const [form, setForm] = useState({
    nickname: "",
    serialNum: "",
    objectName: "",
    objectCode: "DOG",
    objectBirth: "",
  });

  const [serialChecked, setSerialChecked] = useState<"none" | "checking" | "valid" | "in_use" | "not_exist">("none");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleVerifySerial = async () => {
    if (!form.serialNum.trim()) {
      toast.error("시리얼 번호를 입력해주세요.");
      return;
    }
    
    setSerialChecked("checking");
    try {
      const status = await checkSerial(form.serialNum.trim());
      if (status === "ok") {
        setSerialChecked("valid");
        toast.success("사용 가능한 시리얼 번호입니다!");
      } else if (status === "in_use") {
        setSerialChecked("in_use");
        toast.warning("이미 사용 중인 시리얼 번호입니다.");
      } else {
        setSerialChecked("not_exist");
        toast.error("존재하지 않는 시리얼 번호입니다. 관리자 페이지에서 발급된 번호인지 확인해 주세요.");
      }
    } catch (err) {
      console.error(err);
      setSerialChecked("none");
      toast.error("시리얼 확인 중 오류가 발생했습니다.");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.nickname.trim()) {
      toast.error("하우스 이름을 입력해주세요.");
      return;
    }
    if (serialChecked !== "valid") {
      toast.error("시리얼 번호 중복 확인을 완료해주세요.");
      return;
    }
    if (!form.objectName.trim()) {
      toast.error("반려동물 이름을 입력해주세요.");
      return;
    }
    if (!form.objectBirth) {
      toast.error("반려동물 생일을 선택해주세요.");
      return;
    }

    setIsSubmitting(true);
    try {
      // deviceId는 시리얼 번호 기반으로 충돌 방지를 위해 생성합니다
      const deviceId = "DEV-" + form.serialNum.trim();
      
      await createDevice({
        deviceId,
        memberId: memberId || "",
        serialNum: form.serialNum.trim(),
        deviceType: "HOUSE",
        objectName: form.objectName.trim(),
        objectBirth: form.objectBirth,
        objectCode: form.objectCode,
        nickname: form.nickname.trim(),
      });

      toast.success("기기가 정상적으로 등록되었습니다!");
      
      if (memberId) {
        await loadDevicesFromServer(memberId);
      }
    } catch (err: any) {
      console.error(err);
      const errMsg = err?.response?.data?.message || "기기 등록에 실패했습니다. 다시 시도해주세요.";
      toast.error(errMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-tr from-slate-100 via-slate-50 to-blue-50/30 flex flex-col justify-center items-center p-4 sm:p-6 lg:p-8 relative overflow-hidden">
      {/* Background blobs */}
      <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-indigo-200/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-purple-200/20 rounded-full blur-3xl pointer-events-none" />

      {/* Top Header */}
      <div className="absolute top-4 right-4 sm:top-6 sm:right-6 flex items-center gap-3 z-10">
        <span className="text-sm font-semibold text-slate-500">{memberId} 님</span>
        <button
          onClick={() => {
            logout();
            toast.success("로그아웃 되었습니다.");
          }}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 text-xs font-semibold shadow-sm transition-all"
        >
          <LogOut className="w-3.5 h-3.5" />
          로그아웃
        </button>
      </div>

      {/* Main Container */}
      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-2xl border border-slate-100 flex flex-col lg:flex-row overflow-hidden min-h-[600px] relative z-0">
        {/* Left Side: Graphic Intro Panel */}
        <div className="lg:w-2/5 bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 p-8 sm:p-12 flex flex-col justify-between text-white relative overflow-hidden">
          <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]" />
          
          <div className="relative z-10 space-y-6">
            <div className="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center backdrop-blur-md">
              <Dog className="w-6 h-6 text-indigo-400" />
            </div>
            <div>
              <span className="text-xs font-bold tracking-widest text-indigo-400 uppercase">IoT Pet House</span>
              <h2 className="text-3xl font-extrabold tracking-tight mt-1 leading-tight">
                첫 펫하우스를<br />등록해 주세요!
              </h2>
            </div>
            <p className="text-slate-400 text-sm leading-relaxed">
              발급 받으신 시리얼 번호를 이용해 첫 기기를 등록하면, 반려동물의 실시간 상태 모니터링 및 급여, 환풍 제어를 바로 이용할 수 있습니다.
            </p>
          </div>

          <div className="relative z-10 mt-8 space-y-4">
            <div className="p-4 bg-white/5 rounded-2xl border border-white/10 backdrop-blur-md flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-green-500/20 flex items-center justify-center text-green-400">
                <Check className="w-4 h-4" />
              </div>
              <div className="text-xs">
                <div className="font-bold text-white">시리얼 번호 필수</div>
                <div className="text-slate-400">관리자 페이지에서 생성된 번호가 필요합니다.</div>
              </div>
            </div>

            <div className="p-4 bg-white/5 rounded-2xl border border-white/10 backdrop-blur-md flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-indigo-500/20 flex items-center justify-center text-indigo-400">
                <Sparkles className="w-4 h-4" />
              </div>
              <div className="text-xs">
                <div className="font-bold text-white">반려동물 건강 관리</div>
                <div className="text-slate-400">기기가 연동되면 통계 분석이 실시간 제공됩니다.</div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Wizard Form Panel */}
        <form onSubmit={handleSubmit} className="lg:w-3/5 p-8 sm:p-12 flex flex-col justify-between bg-white">
          <div className="space-y-6">
            <div>
              <h3 className="text-xl font-bold text-slate-800">펫하우스 및 반려동물 정보 입력</h3>
              <p className="text-slate-400 text-xs mt-1 font-medium">아래의 필수 정보를 정확하게 기입해 주세요.</p>
            </div>

            <div className="space-y-4">
              {/* Pet House Nickname */}
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1.5">펫하우스 이름</label>
                <input
                  type="text"
                  placeholder="예: 거실 펫하우스, 초코의 보금자리"
                  value={form.nickname}
                  onChange={(e) => setForm({ ...form, nickname: e.target.value })}
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all placeholder:text-slate-300 font-semibold"
                  required
                />
              </div>

              {/* Serial Number Field */}
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1.5">시리얼 번호 (Serial Number)</label>
                <div className="flex gap-2">
                  <div className="relative flex-1">
                    <input
                      type="text"
                      placeholder="예: 20251013-DEV-001"
                      value={form.serialNum}
                      onChange={(e) => {
                        setForm({ ...form, serialNum: e.target.value });
                        setSerialChecked("none"); // Reset check state on change
                      }}
                      className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all placeholder:text-slate-300 font-semibold font-mono"
                      required
                    />
                    <Key className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                  </div>
                  <button
                    type="button"
                    onClick={handleVerifySerial}
                    disabled={serialChecked === "checking" || !form.serialNum.trim()}
                    className="px-4 py-2.5 bg-slate-900 text-white rounded-xl text-sm font-semibold hover:bg-slate-800 disabled:bg-slate-100 disabled:text-slate-300 transition-all shadow-sm flex items-center gap-1.5 cursor-pointer whitespace-nowrap"
                  >
                    {serialChecked === "checking" && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                    중복 확인
                  </button>
                </div>

                {/* Validation Status Indicator */}
                {serialChecked !== "none" && (
                  <div className="mt-2 flex items-center gap-1.5 text-xs font-semibold">
                    {serialChecked === "checking" && (
                      <span className="text-slate-400 flex items-center gap-1">
                        <Loader2 className="w-3 h-3 animate-spin" /> 시리얼 번호를 확인하고 있습니다...
                      </span>
                    )}
                    {serialChecked === "valid" && (
                      <span className="text-emerald-600 flex items-center gap-1">
                        <Check className="w-3.5 h-3.5 text-emerald-500" /> 사용 가능한 시리얼 번호입니다! 등록을 진행할 수 있습니다.
                      </span>
                    )}
                    {serialChecked === "in_use" && (
                      <span className="text-amber-600 flex items-center gap-1">
                        <AlertCircle className="w-3.5 h-3.5 text-amber-500" /> 이미 사용 중인 시리얼 번호입니다. 다른 번호를 사용해 주세요.
                      </span>
                    )}
                    {serialChecked === "not_exist" && (
                      <span className="text-rose-600 flex items-center gap-1">
                        <AlertCircle className="w-3.5 h-3.5 text-rose-500" /> 존재하지 않는 시리얼 번호입니다. 번호를 다시 확인해 주세요.
                      </span>
                    )}
                  </div>
                )}
              </div>

              {/* Pet Info Block */}
              <div className="border-t border-slate-100 my-4 pt-4 space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  {/* Pet Name */}
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">반려동물 이름</label>
                    <input
                      type="text"
                      placeholder="예: 초코, 나비"
                      value={form.objectName}
                      onChange={(e) => setForm({ ...form, objectName: e.target.value })}
                      className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all placeholder:text-slate-300 font-semibold"
                      required
                    />
                  </div>

                  {/* Pet Birthdate */}
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">반려동물 생일</label>
                    <div className="relative">
                      <input
                        type="date"
                        value={form.objectBirth}
                        onChange={(e) => setForm({ ...form, objectBirth: e.target.value })}
                        className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-slate-700 font-semibold"
                        required
                      />
                      <Calendar className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                    </div>
                  </div>
                </div>

                {/* Pet Type Select Button Group */}
                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-2">반려동물 종류</label>
                  <div className="grid grid-cols-4 gap-2">
                    {[
                      { code: "DOG", emoji: "🐶", label: "강아지" },
                      { code: "CAT", emoji: "🐱", label: "고양이" },
                      { code: "RABBIT", emoji: "🐰", label: "토끼" },
                      { code: "HAMSTER", emoji: "🐹", label: "햄스터" },
                    ].map((pet) => (
                      <button
                        key={pet.code}
                        type="button"
                        onClick={() => setForm({ ...form, objectCode: pet.code })}
                        className={`py-3 rounded-2xl border text-sm font-semibold flex flex-col items-center justify-center gap-1.5 transition-all shadow-sm cursor-pointer ${
                          form.objectCode === pet.code
                            ? "bg-indigo-50 border-indigo-300 text-indigo-700 ring-2 ring-indigo-500/10 animate-pulse"
                            : "bg-white border-slate-200 hover:bg-slate-50 text-slate-600"
                        }`}
                      >
                        <span className="text-xl">{pet.emoji}</span>
                        <span className="text-xs">{pet.label}</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Submit Button */}
          <div className="mt-8 pt-4 border-t border-slate-100 flex gap-3">
            <button
              type="submit"
              disabled={isSubmitting || serialChecked !== "valid"}
              className="flex-1 py-3.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white rounded-2xl font-bold text-sm shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/30 disabled:bg-slate-100 disabled:from-slate-100 disabled:to-slate-100 disabled:text-slate-300 disabled:shadow-none transition-all duration-300 flex items-center justify-center gap-2 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              등록 완료 및 대시보드 진입
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export function Layout() {
  const location = useLocation();
  const { memberId } = useAuthStore();
  const { houses, activeHouse, setActiveHouse, removeHouse, isLoaded, loadDevicesFromServer } = usePetHouse();

  // Desktop: sidebar open/collapsed, Mobile: overlay open/closed
  const [desktopSidebarOpen, setDesktopSidebarOpen] = useState(true);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [sidebarWidth, setSidebarWidth] = useState(320);
  const isResizing = useRef(false);

  useEffect(() => {
    if (!isLoaded && memberId) {
      loadDevicesFromServer(memberId);
    }
  }, [isLoaded, memberId, loadDevicesFromServer]);

  const startResize = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    isResizing.current = true;
    document.body.style.cursor = "col-resize";
    document.body.style.userSelect = "none";

    const onMouseMove = (ev: MouseEvent) => {
      if (!isResizing.current) return;
      const maxWidth = window.innerWidth * 0.4;
      const newWidth = Math.min(maxWidth, Math.max(160, ev.clientX));
      setSidebarWidth(newWidth);
    };

    const onMouseUp = () => {
      isResizing.current = false;
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
      window.removeEventListener("mousemove", onMouseMove);
      window.removeEventListener("mouseup", onMouseUp);
    };

    window.addEventListener("mousemove", onMouseMove);
    window.addEventListener("mouseup", onMouseUp);
  }, []);

  if (!isLoaded) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50 gap-4">
        <Loader2 className="w-10 h-10 animate-spin text-indigo-600" />
        <p className="text-sm font-semibold text-slate-500">기기 정보를 불러오는 중입니다...</p>
      </div>
    );
  }

  if (houses.length === 0) {
    return <DeviceRegistrationScreen />;
  }

  const handleSelectHouse = (house: PetHouse) => {
    setActiveHouse(house);
    setMobileSidebarOpen(false);
    toast.success(`${house.name}(으)로 전환했습니다`);
  };

  const handleRemoveHouse = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    if (houses.length <= 1) {
      toast.error("최소 1개의 하우스가 필요합니다");
      return;
    }
    const house = houses.find((h) => h.id === id);
    removeHouse(id);
    toast.success(`${house?.name}이(가) 삭제되었습니다`);
  };

  const pageTitle = PAGE_TITLES[location.pathname] ?? "대시보드";
  const activeColors = COLOR_MAP[activeHouse.color];

  const SidebarInner = ({ onClose }: { onClose?: () => void }) => (
    <div className="flex flex-col h-full overflow-y-auto">
      {/* My Pet Houses */}
      <div className="px-3 pt-4 pb-2">
        <div className="flex items-center justify-between px-2 mb-2">
          <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">내 펫하우스</span>
          <button
            onClick={() => { onClose?.(); setShowAddModal(true); }}
            className="w-6 h-6 rounded-md flex items-center justify-center text-gray-400 hover:text-blue-500 hover:bg-blue-50 transition-colors"
            title="하우스 추가"
          >
            <Plus className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="space-y-1">
          {houses.map((house) => {
            const c = COLOR_MAP[house.color];
            const isActive = house.id === activeHouse.id;
            return (
              <div
                key={house.id}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all group text-left cursor-pointer ${
                  isActive
                    ? `${c.light} ${c.text}`
                    : "text-gray-600 hover:bg-gray-100"
                }`}
                onClick={() => handleSelectHouse(house)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => e.key === "Enter" && handleSelectHouse(house)}
              >
                <div className={`w-1 h-8 rounded-full flex-shrink-0 ${isActive ? c.dot : "bg-gray-200 group-hover:bg-gray-300"}`} />
                <span className="text-lg flex-shrink-0">{PET_EMOJI[house.petType]}</span>
                <div className="flex-1 min-w-0">
                  <div className={`text-sm font-semibold truncate leading-tight ${isActive ? c.text : "text-gray-800"}`}>
                    {house.name}
                  </div>
                  {hasPetName(house.petName) && (
                    <div className="text-xs text-gray-400 truncate leading-tight">{house.petName}</div>
                  )}
                </div>
                <div className={`w-2 h-2 rounded-full flex-shrink-0 ${house.online ? "bg-green-400" : "bg-gray-300"}`} />
                {houses.length > 1 && (
                  <button
                    onClick={(e) => handleRemoveHouse(e, house.id)}
                    className="opacity-0 group-hover:opacity-100 p-0.5 rounded hover:bg-red-100 transition-all flex-shrink-0"
                  >
                    <Trash2 className="w-3 h-3 text-red-400" />
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Divider */}
      <div className="mx-4 border-t border-gray-100 my-2" />

      {/* Navigation */}
      <div className="px-3 pb-2">
        <div className="px-2 mb-2">
          <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">메뉴</span>
        </div>
        <div className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => onClose?.()}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all text-sm font-medium ${
                  isActive
                    ? `${activeColors.light} ${activeColors.text}`
                    : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                }`}
              >
                <Icon className={`w-4 h-4 flex-shrink-0 ${isActive ? activeColors.text : "text-gray-400"}`} />
                {item.label}
                {isActive && <ChevronRight className={`w-3.5 h-3.5 ml-auto ${activeColors.text}`} />}
              </Link>
            );
          })}
        </div>
      </div>

      {/* Bottom: active house status */}
      <div className="mt-auto px-3 pb-4 pt-2">
        <div className={`rounded-xl p-3 ${activeColors.light} border ${activeColors.border}`}>
          <div className="flex items-center gap-2">
            <span className="text-lg">{PET_EMOJI[activeHouse.petType]}</span>
            <div className="flex-1 min-w-0">
              <div className={`text-sm font-semibold truncate ${activeColors.text}`}>{activeHouse.name}</div>
              <div className="text-xs text-gray-500 truncate">
                {hasPetName(activeHouse.petName)
                  ? `${activeHouse.petName} · ${PET_TYPE_LABEL[activeHouse.petType]}`
                  : PET_TYPE_LABEL[activeHouse.petType]}
              </div>
            </div>
            <div className={`flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full ${
              activeHouse.online ? "bg-green-100 text-green-700" : "bg-gray-200 text-gray-500"
            }`}>
              {activeHouse.online ? <Wifi className="w-3 h-3" /> : <WifiOff className="w-3 h-3" />}
              {activeHouse.online ? "온라인" : "오프라인"}
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">

      {/* ── Top Header (full width) ── */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-40 flex-shrink-0">
        <div className="flex items-center gap-3 px-4 sm:px-6 h-16">

          {/* Sidebar toggle (desktop) */}
          <button
            onClick={() => setDesktopSidebarOpen((v) => !v)}
            className="hidden lg:flex p-2 rounded-lg hover:bg-gray-100 text-gray-500 transition-colors"
            title={desktopSidebarOpen ? "사이드바 닫기" : "사이드바 열기"}
          >
            {desktopSidebarOpen
              ? <PanelLeftClose className="w-5 h-5" />
              : <PanelLeftOpen className="w-5 h-5" />}
          </button>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMobileSidebarOpen(true)}
            className="lg:hidden p-2 rounded-lg hover:bg-gray-100 text-gray-500 transition-colors"
          >
            <Menu className="w-5 h-5" />
          </button>

          {/* Logo + Title */}
          <Link to="/user" className="flex items-center gap-3 flex-1">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center flex-shrink-0">
              <Dog className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-gray-900 leading-tight" style={{ fontSize: '1rem' }}>
                펫하우스 관리 시스템
              </h1>
              <p className="text-xs text-gray-400 leading-tight">반려동물 건강 모니터링</p>
            </div>
          </Link>

          {/* Active house pill — only shown when sidebar is collapsed (desktop) */}
          {!desktopSidebarOpen && (
            <div className={`hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-xl border text-sm ${activeColors.light} ${activeColors.border}`}>
              <span className="text-base leading-none">{PET_EMOJI[activeHouse.petType]}</span>
              <span className={`font-semibold ${activeColors.text}`}>{activeHouse.name}</span>
              {hasPetName(activeHouse.petName) && (
                <span className="text-gray-400 text-xs">{activeHouse.petName}</span>
              )}
              <div className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${activeHouse.online ? "bg-green-500 animate-pulse" : "bg-gray-400"}`} />
            </div>
          )}

          {/* Connection status */}
          <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border ${
            activeHouse.online
              ? "bg-green-50 text-green-700 border-green-200"
              : "bg-gray-100 text-gray-500 border-gray-200"
          }`}>
            <div className={`w-1.5 h-1.5 rounded-full ${activeHouse.online ? "bg-green-500 animate-pulse" : "bg-gray-400"}`} />
            <span className="hidden sm:inline">{activeHouse.online ? "연결됨" : "오프라인"}</span>
          </div>
        </div>
      </header>

      {/* ── Body (sidebar + content) ── */}
      <div className="flex flex-1 min-h-0">

        {/* Desktop Sidebar */}
        <aside
          className={`hidden lg:flex flex-col bg-white border-r border-gray-100 flex-shrink-0 overflow-hidden relative ${
            desktopSidebarOpen ? "" : "w-0 border-r-0"
          }`}
          style={desktopSidebarOpen ? { width: sidebarWidth } : undefined}
        >
          {desktopSidebarOpen && (
            <>
              <SidebarInner />
              {/* Drag handle */}
              <div
                onMouseDown={startResize}
                className="absolute top-0 right-0 w-1 h-full cursor-col-resize group z-10 hover:bg-blue-400/40 transition-colors"
                title="드래그하여 사이드바 크기 조절"
              />
            </>
          )}
        </aside>

        {/* Mobile Sidebar Overlay */}
        {mobileSidebarOpen && (
          <>
            <div
              className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 lg:hidden"
              onClick={() => setMobileSidebarOpen(false)}
            />
            <aside className="fixed top-16 left-0 bottom-0 w-64 bg-white border-r border-gray-100 z-50 lg:hidden flex flex-col">
              <SidebarInner onClose={() => setMobileSidebarOpen(false)} />
            </aside>
          </>
        )}

        {/* Main content */}
        <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
          {/* Tab navigation bar */}
          <nav className="bg-white border-b border-gray-200 flex-shrink-0">
            <div className="max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8">
              <div className="flex gap-0 overflow-x-auto">
                {NAV_ITEMS.map((item) => {
                  const Icon = item.icon;
                  const isActive = location.pathname === item.path;
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      className={`flex items-center gap-2 px-4 py-3 border-b-2 transition-colors whitespace-nowrap text-sm font-medium ${
                        isActive
                          ? `border-blue-500 ${activeColors.text} bg-blue-50/50`
                          : "border-transparent text-gray-500 hover:text-gray-800 hover:border-gray-300"
                      }`}
                    >
                      <Icon className={`w-4 h-4 flex-shrink-0 ${isActive ? activeColors.text : "text-gray-400"}`} />
                      {item.label}
                    </Link>
                  );
                })}
              </div>
            </div>
          </nav>

          {/* Sub-header: breadcrumb + page title */}
          <div className="bg-white border-b border-gray-100 flex-shrink-0">
            <div className="max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 h-11 flex items-center gap-2">
              <span className="text-gray-400 text-sm hidden sm:inline">{activeHouse.name}</span>
              <ChevronRight className="w-3.5 h-3.5 text-gray-300 hidden sm:inline" />
              <span className="font-semibold text-gray-800 text-sm">{pageTitle}</span>
            </div>
          </div>

          {/* Page content */}
          <main className="flex-1 overflow-auto py-6">
            <div className="max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8">
              <Outlet />
            </div>
          </main>
        </div>
      </div>

      {/* Add House Modal */}
      {showAddModal && <AddHouseModal onClose={() => setShowAddModal(false)} />}
    </div>
  );
}