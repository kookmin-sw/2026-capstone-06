import { useState, useEffect, useMemo, useCallback } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import { Card, CardContent } from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import {
    MapPin,
    Phone,
    Search,
    Navigation,
    ChevronLeft,
    Clock,
    Stethoscope,
    AlertCircle,
    Loader2,
    X,
    List,
    Map as MapIcon,
    ChevronRight,
} from "lucide-react";
import { toast } from "sonner";
import { getHospitalList, getHospitalDetail } from "../../services/hospitalApi";

// ─── Types ───────────────────────────────────────────────────────────────────

interface Hospital {
    seq: number;
    name: string;
    location: string;
    phone: string;
    latitude: number;
    longitude: number;
    mainMedCode: string;
    regDate: string;
}

interface MedItem {
    hospitalSeq?: number; // API 응답에는 포함되지 않을 수 있음
    medCode: string;
}

interface HospitalWithDistance extends Hospital {
    distance: number; // km
}

// ─── Constants ────────────────────────────────────────────────────────────────

const MED_CODE_MAP: Record<string, { label: string; color: string; bg: string; border: string }> = {
    med01: { label: "일반진료", color: "text-blue-700", bg: "bg-blue-100", border: "border-blue-200" },
    med02: { label: "외과", color: "text-red-700", bg: "bg-red-100", border: "border-red-200" },
    med03: { label: "피부과", color: "text-pink-700", bg: "bg-pink-100", border: "border-pink-200" },
    med04: { label: "안과", color: "text-purple-700", bg: "bg-purple-100", border: "border-purple-200" },
    med05: { label: "치과", color: "text-yellow-700", bg: "bg-yellow-100", border: "border-yellow-200" },
    med06: { label: "정형외과", color: "text-orange-700", bg: "bg-orange-100", border: "border-orange-200" },
    med07: { label: "응급의학", color: "text-rose-700", bg: "bg-rose-100", border: "border-rose-200" },
};

// Default center (Bundang area matches mock data)
const DEFAULT_CENTER: [number, number] = [37.3630, 127.1080];

// ─── Helper functions ─────────────────────────────────────────────────────────

function haversine(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function formatDistance(km: number): string {
    if (km < 1) return `${Math.round(km * 1000)}m`;
    return `${km.toFixed(1)}km`;
}

// ─── Custom Leaflet icons ─────────────────────────────────────────────────────

const createHospitalIcon = (isSelected: boolean, isEmergency: boolean) =>
    L.divIcon({
        html: `
      <div style="
        position: relative;
        width: 36px; height: 44px;
      ">
        <div style="
          width: 36px; height: 36px;
          background: ${isSelected ? "#16a34a" : isEmergency ? "#dc2626" : "#2563eb"};
          border: 2.5px solid white;
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          box-shadow: 0 3px 8px rgba(0,0,0,0.35);
          display: flex; align-items: center; justify-content: center;
        ">
          <span style="transform: rotate(45deg); font-size: 16px; line-height: 1;">🏥</span>
        </div>
      </div>
    `,
        className: "",
        iconSize: [36, 44],
        iconAnchor: [18, 44],
        popupAnchor: [0, -46],
    });

const userIcon = L.divIcon({
    html: `
    <div style="
      width: 18px; height: 18px;
      background: #3b82f6;
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 0 0 5px rgba(59,130,246,0.25), 0 2px 8px rgba(0,0,0,0.25);
    "></div>
  `,
    className: "",
    iconSize: [18, 18],
    iconAnchor: [9, 9],
});

// ─── MapController (handles flyTo) ───────────────────────────────────────────

function MapController({
    center,
    zoom,
}: {
    center: [number, number];
    zoom: number;
}) {
    const map = useMap();
    useEffect(() => {
        // Validate coordinates before flyTo
        if (isNaN(center[0]) || isNaN(center[1]) || !isFinite(center[0]) || !isFinite(center[1])) {
            console.warn('[MapController] Invalid coordinates:', center);
            return;
        }
        map.flyTo(center, zoom, { animate: true, duration: 0.8 });
    }, [center, zoom, map]);
    return null;
}

// ─── MedBadge ─────────────────────────────────────────────────────────────────

function MedBadge({ code }: { code: string }) {
    const meta = MED_CODE_MAP[code] ?? { label: code, color: "text-gray-600", bg: "bg-gray-100", border: "border-gray-200" };
    return (
        <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border ${meta.bg} ${meta.color} ${meta.border}`}>
            {code === "med07" && <AlertCircle className="w-3 h-3" />}
            {meta.label}
        </span>
    );
}

// ─── Hospital Detail Panel ───────────────────────────────────────────────────

function HospitalDetail({
    hospital,
    distance,
    medList,
    onBack,
}: {
    hospital: Hospital;
    distance: number;
    medList: MedItem[];
    onBack: () => void;
}) {

    return (
        <div className="flex flex-col h-full">
            {/* Back button */}
            <div className="flex items-center gap-2 px-4 py-3 border-b border-gray-100 shrink-0">
                <Button variant="ghost" size="sm" onClick={onBack} className="gap-1.5 px-2 text-gray-600">
                    <ChevronLeft className="w-4 h-4" />
                    목록으로
                </Button>
            </div>

            <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
                {/* Hospital name + distance */}
                <div>
                    <div className="flex items-start justify-between gap-2">
                        <h3 className="font-bold text-gray-900 text-lg leading-tight">{hospital.name}</h3>
                        <span className="shrink-0 text-sm font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full border border-blue-100">
                            {formatDistance(distance)}
                        </span>
                    </div>
                    <div className="flex flex-wrap gap-1.5 mt-2">
                        <MedBadge code={hospital.mainMedCode} />
                        {hospital.mainMedCode === "med07" && (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border border-red-300 bg-red-50 text-red-700">
                                <Clock className="w-3 h-3" /> 24시간
                            </span>
                        )}
                    </div>
                </div>

                {/* Info rows */}
                <div className="space-y-2">
                    <div className="flex items-start gap-2.5 p-3 bg-gray-50 rounded-xl">
                        <MapPin className="w-4 h-4 text-gray-400 mt-0.5 shrink-0" />
                        <span className="text-sm text-gray-700">{hospital.location}</span>
                    </div>
                    <a
                        href={`tel:${hospital.phone}`}
                        className="flex items-center gap-2.5 p-3 bg-green-50 border border-green-100 rounded-xl hover:bg-green-100 transition-colors"
                    >
                        <Phone className="w-4 h-4 text-green-600 shrink-0" />
                        <span className="text-sm font-semibold text-green-700">{hospital.phone}</span>
                        <span className="ml-auto text-xs text-green-600 font-medium">전화걸기 →</span>
                    </a>
                    <div className="flex items-center gap-2.5 p-3 bg-gray-50 rounded-xl">
                        <Clock className="w-4 h-4 text-gray-400 shrink-0" />
                        <span className="text-sm text-gray-600">등록일: {new Date(hospital.regDate).toLocaleDateString("ko-KR")}</span>
                    </div>
                </div>

                {/* Specialties */}
                <div>
                    <div className="flex items-center gap-2 mb-2.5">
                        <Stethoscope className="w-4 h-4 text-blue-500" />
                        <span className="text-sm font-semibold text-gray-700">진료과목</span>
                        <span className="text-xs text-gray-400">({medList.length}개)</span>
                    </div>
                    <div className="flex flex-wrap gap-1.5 p-3 bg-blue-50 border border-blue-100 rounded-xl">
                        {medList.map((item) => (
                            <MedBadge key={item.medCode} code={item.medCode} />
                        ))}
                    </div>
                </div>

                {/* Raw API data preview */}
                <details className="group">
                    <summary className="text-xs text-gray-400 cursor-pointer hover:text-gray-600 flex items-center gap-1">
                        <ChevronRight className="w-3 h-3 group-open:rotate-90 transition-transform" />
                        API 응답 데이터 보기
                    </summary>
                    <pre className="mt-2 p-3 bg-gray-900 text-green-400 rounded-lg text-xs overflow-x-auto">
                        {JSON.stringify({ hospital, medList }, null, 2)}
                    </pre>
                </details>
            </div>
        </div>
    );
}

// ─── Hospital Card ────────────────────────────────────────────────────────────

function HospitalCard({
    hospital,
    isSelected,
    onClick,
}: {
    hospital: HospitalWithDistance;
    isSelected: boolean;
    onClick: () => void;
}) {
    return (
        <div
            onClick={onClick}
            className={`p-3.5 rounded-xl border-2 cursor-pointer transition-all ${isSelected
                ? "border-blue-400 bg-blue-50 shadow-sm"
                : "border-gray-100 bg-white hover:border-blue-200 hover:bg-blue-50/30"
                }`}
        >
            <div className="flex items-start justify-between gap-2 mb-2">
                <div className="flex items-center gap-2 min-w-0">
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${isSelected ? "bg-blue-500" : "bg-gray-100"
                        }`}>
                        <span className="text-base">🏥</span>
                    </div>
                    <span className={`font-semibold text-sm truncate ${isSelected ? "text-blue-700" : "text-gray-900"}`}>
                        {hospital.name}
                    </span>
                </div>
                <span className={`shrink-0 text-xs font-bold px-2 py-0.5 rounded-full ${hospital.distance < 1
                    ? "bg-green-100 text-green-700"
                    : hospital.distance < 3
                        ? "bg-blue-100 text-blue-700"
                        : "bg-gray-100 text-gray-600"
                    }`}>
                    {formatDistance(hospital.distance)}
                </span>
            </div>

            <div className="flex items-start gap-1.5 text-xs text-gray-500 mb-2">
                <MapPin className="w-3 h-3 shrink-0 mt-0.5 text-gray-400" />
                <span className="line-clamp-1">{hospital.location}</span>
            </div>

            <div className="flex items-center justify-between">
                <div className="flex gap-1 flex-wrap">
                    <MedBadge code={hospital.mainMedCode} />
                    {hospital.mainMedCode === "med07" && (
                        <span className="inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded-full text-xs font-medium border border-red-200 bg-red-50 text-red-600">
                            <Clock className="w-2.5 h-2.5" /> 24시간
                        </span>
                    )}
                </div>
                <span className="text-xs text-gray-400 flex items-center gap-0.5">
                    <Phone className="w-3 h-3" />
                    {hospital.phone}
                </span>
            </div>
        </div>
    );
}

// ─── Main Hospital Page ───────────────────────────────────────────────────────

export function HospitalPage() {
    const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
    const [locationLoading, setLocationLoading] = useState(false);
    const [locationError, setLocationError] = useState(false);
    const [selectedHospital, setSelectedHospital] = useState<HospitalWithDistance | null>(null);
    const [search, setSearch] = useState("");
    const [selectedMedCode, setSelectedMedCode] = useState<string>("all");
    const [mobileView, setMobileView] = useState<"list" | "map">("list");
    const [mapCenter, setMapCenter] = useState<[number, number]>(DEFAULT_CENTER);
    const [mapZoom, setMapZoom] = useState(14);

    // API에서 가져온 병원 데이터
    const [hospitals, setHospitals] = useState<Hospital[]>([]);
    const [apiMedList, setApiMedList] = useState<Record<number, MedItem[]>>({});

    // API에서 병원 목록 가져오기
    useEffect(() => {
        const fetchHospitals = async () => {
            try {
                const page = await getHospitalList({ size: 100 });
                if (page.content && page.content.length > 0) {
                    // Filter out hospitals with invalid coordinates
                    const validHospitals = page.content.filter(h => 
                        !isNaN(h.latitude) && !isNaN(h.longitude) &&
                        isFinite(h.latitude) && isFinite(h.longitude)
                    );
                    if (validHospitals.length > 0) {
                        setHospitals(validHospitals);
                    } else {
                        console.warn('[Hospital] All hospitals have invalid coordinates');
                    }
                }
            } catch {
                // API 실패 시 기존 mock 데이터 유지
                console.info('[Hospital] API 미연결 - Mock 데이터 사용');
            }
        };
        fetchHospitals();
    }, []);

    // 병원 상세(진료과목) 정보 가져오기
    const fetchMedList = useCallback(async (seq: number) => {
        if (apiMedList[seq]) return;
        try {
            const detail = await getHospitalDetail(seq);
            if (detail.medList) {
                setApiMedList(prev => ({ ...prev, [seq]: detail.medList }));
            }
        } catch (error) {
            console.error('[Hospital] Med Detail Fetch Error:', error);
        }
    }, [apiMedList]);

    // Calculate distances
    const hospitalsWithDistance = useMemo<HospitalWithDistance[]>(() => {
        const center = userLocation ?? DEFAULT_CENTER;
        return hospitals.map((h) => ({
            ...h,
            distance: haversine(center[0], center[1], h.latitude, h.longitude),
        })).sort((a, b) => a.distance - b.distance);
    }, [userLocation, hospitals]);

    // Filtered list
    const filtered = useMemo<HospitalWithDistance[]>(() => {
        return hospitalsWithDistance.filter((h) => {
            const matchSearch =
                search === "" ||
                h.name.includes(search) ||
                h.location.includes(search);
            const matchMed =
                selectedMedCode === "all" ||
                (apiMedList[h.seq] ?? []).some((m) => m.medCode === selectedMedCode);
            return matchSearch && matchMed;
        });
    }, [hospitalsWithDistance, search, selectedMedCode, apiMedList]);

    const isHospitalSelected = (h: HospitalWithDistance) => {
        return !!selectedHospital && selectedHospital.seq === h.seq;
    };

    const handleGetLocation = () => {
        if (!navigator.geolocation) {
            toast.error("이 브라우저는 위치 서비스를 지원하지 않습니다");
            return;
        }
        setLocationLoading(true);
        setLocationError(false);
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const loc: [number, number] = [pos.coords.latitude, pos.coords.longitude];
                setUserLocation(loc);
                setMapCenter(loc);
                setMapZoom(14);
                setLocationLoading(false);
                toast.success("내 위치를 확인했습니다");
            },
            () => {
                setLocationLoading(false);
                setLocationError(true);
                toast.error("위치 정보를 가져올 수 없습니다. 브라우저 위치 권한을 확인해주세요.");
            },
            { timeout: 10000 }
        );
    };

    const handleSelectHospital = (hospital: HospitalWithDistance) => {
        setSelectedHospital(hospital);
        // Validate coordinates before setting map center
        if (!isNaN(hospital.latitude) && !isNaN(hospital.longitude) && 
            isFinite(hospital.latitude) && isFinite(hospital.longitude)) {
            setMapCenter([hospital.latitude, hospital.longitude]);
            setMapZoom(16);
        } else {
            console.warn('[Hospital] Invalid hospital coordinates:', hospital);
            toast.error("병원 위치 정보가 올바르지 않습니다");
            return;
        }
        setMobileView("map");
        fetchMedList(hospital.seq); // API에서 진료과목 상세 가져오기
    };

    const handleBack = () => {
        setSelectedHospital(null);
        setMapZoom(14);
        if (userLocation) setMapCenter(userLocation);
        else setMapCenter(DEFAULT_CENTER);
    };

    const uniqueMedCodes = useMemo(() => {
        const codes = new Set<string>();
        hospitals.forEach((h) => {
            (apiMedList[h.seq] ?? []).forEach((m) => codes.add(m.medCode));
        });
        // 만약 로딩 전이라 데이터가 없다면 기본 진료과목들 표시
        if (codes.size === 0) return Object.keys(MED_CODE_MAP).sort();
        return Array.from(codes).sort();
    }, [hospitals, apiMedList]);

    // ── Render ──────────────────────────────────────────────────────────────────
    return (
        <div className="space-y-4">
            {/* Page header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                    <h2 className="text-3xl font-bold text-gray-900">내 근처 동물병원</h2>
                    <p className="text-gray-500 mt-1.5 text-sm">
                        {userLocation
                            ? "현재 위치 기준으로 가까운 병원을 표시합니다"
                            : "위치를 허용하면 가까운 순서로 정렬됩니다"}
                    </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                    {locationError && (
                        <span className="text-xs text-red-600 flex items-center gap-1">
                            <AlertCircle className="w-3.5 h-3.5" /> 위치 권한 필요
                        </span>
                    )}
                    <Button
                        onClick={handleGetLocation}
                        disabled={locationLoading}
                        className={`gap-2 ${userLocation ? "bg-green-600 hover:bg-green-700" : ""}`}
                    >
                        {locationLoading ? (
                            <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                            <Navigation className="w-4 h-4" />
                        )}
                        {locationLoading ? "위치 확인 중..." : userLocation ? "위치 갱신" : "내 위치 찾기"}
                    </Button>
                </div>
            </div>

            {/* Stats bar */}
            <div className="flex gap-3 flex-wrap">
                <div className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs font-medium text-gray-600">
                    <span className="w-2 h-2 rounded-full bg-blue-500" />
                    전체 {filtered.length}개 병원
                </div>
                <div className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs font-medium text-gray-600">
                    <span className="w-2 h-2 rounded-full bg-red-500" />
                    응급 {filtered.filter(h => h.mainMedCode === "med07").length}개
                </div>
                {userLocation && (
                    <div className="flex items-center gap-1.5 px-3 py-1.5 bg-green-50 border border-green-200 rounded-full text-xs font-medium text-green-700">
                        <Navigation className="w-3 h-3" />
                        위치 확인됨
                    </div>
                )}
            </div>

            {/* Mobile view toggle */}
            <div className="flex lg:hidden bg-gray-100 rounded-xl p-1 w-fit gap-1">
                <button
                    onClick={() => setMobileView("list")}
                    className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-all ${mobileView === "list" ? "bg-white text-blue-700 shadow-sm" : "text-gray-500"
                        }`}
                >
                    <List className="w-4 h-4" /> 목록
                </button>
                <button
                    onClick={() => setMobileView("map")}
                    className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-all ${mobileView === "map" ? "bg-white text-blue-700 shadow-sm" : "text-gray-500"
                        }`}
                >
                    <MapIcon className="w-4 h-4" /> 지도
                </button>
            </div>

            {/* ── Main split layout ── */}
            <div className="flex gap-4" style={{ height: "calc(100vh - 320px)", minHeight: "520px" }}>

                {/* ── Left panel ── */}
                <div className={`shrink-0 flex flex-col bg-white rounded-xl border border-gray-200 overflow-hidden w-full lg:w-85 xl:w-95 ${mobileView === "map" ? "hidden lg:flex" : "flex"
                    }`}>
                    {selectedHospital ? (
                        // Detail view
                        <HospitalDetail
                            hospital={selectedHospital}
                            distance={selectedHospital.distance}
                            medList={apiMedList[selectedHospital.seq] ?? []}
                            onBack={handleBack}
                        />
                    ) : (
                        // List view
                        <>
                            {/* Search + filters */}
                            <div className="px-4 pt-4 pb-3 border-b border-gray-100 space-y-3 shrink-0">
                                <div className="relative">
                                    <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                    <Input
                                        placeholder="병원명 또는 주소 검색..."
                                        value={search}
                                        onChange={(e) => setSearch(e.target.value)}
                                        className="pl-9 pr-8 h-9 text-sm"
                                    />
                                    {search && (
                                        <button
                                            onClick={() => setSearch("")}
                                            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                        >
                                            <X className="w-3.5 h-3.5" />
                                        </button>
                                    )}
                                </div>
                                {/* Specialty filter */}
                                <div className="flex gap-1.5 flex-wrap">
                                    <button
                                        onClick={() => setSelectedMedCode("all")}
                                        className={`px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selectedMedCode === "all"
                                            ? "bg-blue-600 text-white border-blue-600"
                                            : "bg-white text-gray-600 border-gray-200 hover:border-blue-300"
                                            }`}
                                    >
                                        전체
                                    </button>
                                    {uniqueMedCodes.map((code) => {
                                        const meta = MED_CODE_MAP[code];
                                        return (
                                            <button
                                                key={code}
                                                onClick={() => setSelectedMedCode(code === selectedMedCode ? "all" : code)}
                                                className={`px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${selectedMedCode === code
                                                    ? `${meta.bg} ${meta.color} ${meta.border}`
                                                    : "bg-white text-gray-600 border-gray-200 hover:border-gray-300"
                                                    }`}
                                            >
                                                {meta?.label ?? code}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Hospital list */}
                            <div className="flex-1 overflow-y-auto">
                                {filtered.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center h-full gap-3 text-gray-400">
                                        <Search className="w-10 h-10 opacity-40" />
                                        <div className="text-sm">검색 결과가 없습니다</div>
                                    </div>
                                ) : (
                                    <div className="p-3 space-y-2">
                                        {filtered.map((hospital: HospitalWithDistance, idx) => (
                                            <div key={hospital.seq}>
                                                {/* Distance separator */}
                                                {idx > 0 &&
                                                    filtered[idx - 1].distance < 1 &&
                                                    hospital.distance >= 1 && (
                                                        <div className="flex items-center gap-2 py-1.5">
                                                            <div className="flex-1 h-px bg-gray-100" />
                                                            <span className="text-xs text-gray-400">1km 이상</span>
                                                            <div className="flex-1 h-px bg-gray-100" />
                                                        </div>
                                                    )}
                                                {idx > 0 &&
                                                    filtered[idx - 1].distance < 3 &&
                                                    hospital.distance >= 3 && (
                                                        <div className="flex items-center gap-2 py-1.5">
                                                            <div className="flex-1 h-px bg-gray-100" />
                                                            <span className="text-xs text-gray-400">3km 이상</span>
                                                            <div className="flex-1 h-px bg-gray-100" />
                                                        </div>
                                                    )}
                                                <HospitalCard
                                                    hospital={hospital}
                                                    isSelected={isHospitalSelected(hospital)}
                                                    onClick={() => handleSelectHospital(hospital)}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            <div className="px-4 py-2 border-t border-gray-100 text-xs text-gray-400 text-center shrink-0">
                                {userLocation ? "📍 현재 위치 기준 거리순 정렬" : "📍 기본 위치 기준 (분당구)"}
                            </div>
                        </>
                    )}
                </div>

                {/* ── Right panel: Map ── */}
                <div className={`flex-1 rounded-xl overflow-hidden border border-gray-200 relative ${mobileView === "list" ? "hidden lg:block" : "block"
                    }`}>
                    <MapContainer
                        center={DEFAULT_CENTER}
                        zoom={14}
                        style={{ width: "100%", height: "100%" }}
                        zoomControl={true}
                    >
                        <MapController center={mapCenter} zoom={mapZoom} />
                        <TileLayer
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />

                        {/* User location marker */}
                        {userLocation && (
                            <Marker position={userLocation} icon={userIcon}>
                                <Popup>
                                    <div className="text-sm font-semibold text-blue-700">📍 내 위치</div>
                                </Popup>
                            </Marker>
                        )}

                        {/* Hospital markers */}
                        {filtered.map((hospital) => {
                            // Skip markers with invalid coordinates
                            if (isNaN(hospital.latitude) || isNaN(hospital.longitude) ||
                                !isFinite(hospital.latitude) || !isFinite(hospital.longitude)) {
                                return null;
                            }
                            return (
                            <Marker
                                key={hospital.seq}
                                position={[hospital.latitude, hospital.longitude]}
                                icon={createHospitalIcon(
                                    selectedHospital?.seq === hospital.seq,
                                    hospital.mainMedCode === "med07"
                                )}
                                eventHandlers={{
                                    click: () => handleSelectHospital(hospital),
                                }}
                            >
                                <Popup>
                                    <div className="min-w-40">
                                        <div className="font-bold text-gray-900 mb-1">{hospital.name}</div>
                                        <div className="text-xs text-gray-500 mb-1.5">{hospital.location}</div>
                                        <div className="flex items-center justify-between gap-2">
                                            <MedBadge code={hospital.mainMedCode} />
                                            <span className="text-xs font-semibold text-blue-600">
                                                {formatDistance(hospital.distance)}
                                            </span>
                                        </div>
                                        <div className="text-xs text-gray-600 mt-1.5 flex items-center gap-1">
                                            <Phone className="w-3 h-3" />
                                            {hospital.phone}
                                        </div>
                                        <button
                                            onClick={() => handleSelectHospital(hospital)}
                                            className="mt-2 w-full text-xs bg-blue-600 text-white py-1.5 rounded-lg font-medium hover:bg-blue-700"
                                        >
                                            자세히 보기
                                        </button>
                                    </div>
                                </Popup>
                            </Marker>
                            );
                        })}
                    </MapContainer>

                    {/* Map overlay legend */}
                    <div className="absolute bottom-4 right-4 z-1000 bg-white/95 backdrop-blur-sm rounded-xl shadow-lg border border-gray-200 p-3 text-xs space-y-1.5">
                        <div className="font-semibold text-gray-700 mb-2">범례</div>
                        <div className="flex items-center gap-2">
                            <div className="w-4 h-4 rounded-full bg-blue-500 border-2 border-white shadow-sm shrink-0" />
                            <span className="text-gray-600">내 위치</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-4 h-4 rounded-sm bg-blue-600 shrink-0" />
                            <span className="text-gray-600">일반 병원</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-4 h-4 rounded-sm bg-red-600 shrink-0" />
                            <span className="text-gray-600">응급 병원</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-4 h-4 rounded-sm bg-green-600 shrink-0" />
                            <span className="text-gray-600">선택된 병원</span>
                        </div>
                    </div>

                    {/* Selected hospital quick info overlay (map view only) */}
                    {selectedHospital && mobileView === "map" && (
                        <div className="absolute bottom-4 left-4 right-20 z-1000 lg:hidden">
                            <Card className="shadow-xl border-blue-200 bg-white/98 backdrop-blur-sm">
                                <CardContent className="p-3">
                                    <div className="flex items-start gap-2">
                                        <div className="flex-1 min-w-0">
                                            <div className="font-bold text-sm text-gray-900 truncate">{selectedHospital.name}</div>
                                            <div className="text-xs text-gray-500 truncate mt-0.5">{selectedHospital.location}</div>
                                            <div className="flex items-center gap-1.5 mt-1">
                                                <MedBadge code={selectedHospital.mainMedCode} />
                                                <span className="text-xs font-semibold text-blue-600">
                                                    {formatDistance(selectedHospital.distance)}
                                                </span>
                                            </div>
                                        </div>
                                        <div className="flex flex-col gap-1 shrink-0">
                                            <button
                                                onClick={() => setMobileView("list")}
                                                className="text-xs bg-blue-600 text-white px-2 py-1 rounded-lg"
                                            >
                                                상세보기
                                            </button>
                                            <button
                                                onClick={handleBack}
                                                className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-lg"
                                            >
                                                닫기
                                            </button>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
