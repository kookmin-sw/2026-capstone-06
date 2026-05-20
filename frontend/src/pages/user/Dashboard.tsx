import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { 
  Droplets, 
  Thermometer, 
  Wind as WindIcon,
  AlertTriangle,
  CheckCircle2,
  Video,
  Camera,
  Maximize2,
  RotateCw,
  Mic,
  Send
} from "lucide-react";
import { toast } from "sonner";
import { getLatestSensorData, getDashboardActivities, getDashboardStats } from "../../services/dashboardApi";
import { usePetHouse } from "../../store/petStore";
import type { ActivityRes, DailyStatsRes } from "../../types/api";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const EMPTY_SENSOR_DATA = {
  co2: null as number | null,
  temperature: null as number | null,
  humidity: null as number | null,
  petPresent: false,
};

function formatSensorValue(value: number | null, decimals: number): string {
  if (value === null) return "-";
  return value.toFixed(decimals);
}

export function Dashboard() {
  const { activeHouse } = usePetHouse();

  const [currentData, setCurrentData] = useState(EMPTY_SENSOR_DATA);

  const [cctvConnected] = useState(true);
  const [sendingVoice, setSendingVoice] = useState(false);
  const [stats, setStats] = useState<DailyStatsRes | null>(null);
  const [activities, setActivities] = useState<ActivityRes[]>([]);
  const [timeFilter, setTimeFilter] = useState<'1h' | '24h' | '1m' | '3m' | 'all'>('all');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  useEffect(() => {
    setCurrentPage(1);
  }, [timeFilter, activeHouse.id]);

  useEffect(() => {
    setCurrentData(EMPTY_SENSOR_DATA);
  }, [activeHouse.id, activeHouse.deviceId]);

  const getFilteredActivities = () => {
    if (timeFilter === 'all') return activities;

    const now = new Date();
    return activities.filter((activity) => {
      const activityDate = new Date(activity.timestamp);
      const diffMs = now.getTime() - activityDate.getTime();
      const diffHours = diffMs / (1000 * 60 * 60);

      if (timeFilter === '1h') {
        return diffHours <= 1;
      }
      if (timeFilter === '24h') {
        return diffHours <= 24;
      }
      if (timeFilter === '1m') {
        return diffHours <= 24 * 30;
      }
      if (timeFilter === '3m') {
        return diffHours <= 24 * 90;
      }
      return true;
    });
  };

  const filteredActivities = getFilteredActivities();
  const totalPages = Math.max(1, Math.ceil(filteredActivities.length / itemsPerPage));
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedActivities = filteredActivities.slice(startIndex, startIndex + itemsPerPage);

  useEffect(() => {
    const targetDeviceId = activeHouse.deviceId || "PET-HOUSE-01";

    const fetchSensorData = async () => {
      try {
        const data = await getLatestSensorData(targetDeviceId);
        if (!data) return;

        const hasSensorReading =
          data.co2 != null || data.temperature != null || data.humidity != null;
        if (!hasSensorReading) return;

        setCurrentData((prev) => ({
          co2: data.co2 ?? prev.co2,
          temperature: data.temperature ?? prev.temperature,
          humidity: data.humidity ?? prev.humidity,
          petPresent: prev.petPresent,
        }));
      } catch (error) {
        console.error('[Dashboard] API Fetch Error:', error);
      }
    };

    const fetchExtraData = async () => {
      try {
        const [activitiesData, statsData] = await Promise.all([
          getDashboardActivities(targetDeviceId),
          getDashboardStats(targetDeviceId)
        ]);
        setActivities(activitiesData);
        setStats(statsData);
      } catch (error) {
        console.error('[Dashboard] Extra API Fetch Error:', error);
      }
    };

    fetchSensorData();
    fetchExtraData();

    // WebSocket STOMP Client Setup
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8081';
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl + '/api/ws'),
      debug: (str) => console.log('[STOMP]', str),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('[STOMP] Connected to WebSocket');
        stompClient.subscribe(`/topic/sensor/house/${targetDeviceId}`, (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body);
              console.log('[STOMP] Parsed data:', data); // 🔍 WebSocket 데이터 확인
              setCurrentData((prev) => ({
                co2: data.coVal ?? prev.co2,
                temperature: data.temVal ?? prev.temperature,
                humidity: data.humVal ?? prev.humidity,
                petPresent: prev.petPresent,
              }));
            } catch (err) {
              console.error('[STOMP] Parse Error:', err);
            }
          }
        });
      },
      onStompError: (frame) => {
        console.error('[STOMP] Broker reported error:', frame.headers['message']);
        console.error('[STOMP] Details:', frame.body);
      },
    });

    stompClient.activate();

    const interval = setInterval(() => {
      fetchExtraData();
    }, 10000);

    return () => {
      clearInterval(interval);
      stompClient.deactivate();
    };
  }, [activeHouse.id, activeHouse.deviceId]);

  const handleSendOwnerVoice = () => {
    setSendingVoice(true);
    toast.success("주인 음성 전송 중...");
    setTimeout(() => {
      setSendingVoice(false);
      toast.success("음성이 펫하우스로 전송되었습니다");
    }, 2000);
  };

  const getStatusColor = (value: number | null, type: string) => {
    if (value === null) {
      return { bg: 'bg-gray-50', border: 'border-gray-200', text: 'text-gray-500', status: '-' };
    }
    if (type === 'co2') {
      if (value > 600) return { bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-700', status: '위험' };
      if (value > 500) return { bg: 'bg-yellow-50', border: 'border-yellow-200', text: 'text-yellow-700', status: '주의' };
      return { bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-700', status: '정상' };
    }
    if (type === 'temp') {
      if (value > 26 || value < 18) return { bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-700', status: '위험' };
      if (value > 24 || value < 20) return { bg: 'bg-yellow-50', border: 'border-yellow-200', text: 'text-yellow-700', status: '주의' };
      return { bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-700', status: '정상' };
    }
    if (type === 'humidity') {
      if (value > 70 || value < 30) return { bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-700', status: '위험' };
      if (value > 65 || value < 40) return { bg: 'bg-yellow-50', border: 'border-yellow-200', text: 'text-yellow-700', status: '주의' };
      return { bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-700', status: '정상' };
    }
    return { bg: 'bg-gray-50', border: 'border-gray-200', text: 'text-gray-700', status: '정상' };
  };

  const co2Status = getStatusColor(currentData.co2, 'co2');
  const tempStatus = getStatusColor(currentData.temperature, 'temp');
  const humidityStatus = getStatusColor(currentData.humidity, 'humidity');

  return (
    <div className="space-y-6">
      {/* Status Banner */}
      <div className="bg-linear-to-r from-blue-500 to-purple-600 rounded-xl p-6 text-white">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold mb-2">실시간 모니터링</h2>
            <p className="text-blue-100">펫하우스 내부 환경을 실시간으로 확인하세요</p>
          </div>
          <div className="flex items-center gap-2">
            {currentData.petPresent ? (
              <>
                <CheckCircle2 className="w-6 h-6" />
                <span className="font-semibold">반려동물 감지됨</span>
              </>
            ) : (
              <>
                <AlertTriangle className="w-6 h-6" />
                <span className="font-semibold">반려동물 없음</span>
              </>
            )}
          </div>
        </div>
      </div>

      {/* CCTV Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="border-2 border-purple-200">
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <CardTitle className="flex items-center gap-2">
              <Video className="w-5 h-5 text-purple-600" />
              실시간 CCTV
            </CardTitle>
            <div className="flex items-center gap-2">
              <Badge className="bg-red-500 text-white animate-pulse">
                <div className="w-2 h-2 bg-white rounded-full mr-2"></div>
                LIVE
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className="relative aspect-video bg-gray-900 rounded-lg overflow-hidden">
              {cctvConnected ? (
                <>
                  <img 
                    src="https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800&auto=format&fit=crop"
                    alt="CCTV Feed"
                    className="w-full h-full object-cover"
                  />
                  {/* CCTV Overlay */}
                  <div className="absolute inset-0 bg-linear-to-b from-black/40 via-transparent to-black/40 pointer-events-none">
                    <div className="absolute top-3 left-3 flex items-center gap-2">
                      <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse"></div>
                      <span className="text-white text-sm font-mono">REC</span>
                    </div>
                    <div className="absolute top-3 right-3 text-white text-sm font-mono bg-black/50 px-2 py-1 rounded">
                      {new Date().toLocaleTimeString('ko-KR')}
                    </div>
                    <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between">
                      <span className="text-white text-xs font-mono">CAM-01 | 펫하우스 내부</span>
                      <span className="text-white text-xs font-mono">1080p</span>
                    </div>
                  </div>
                </>
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <div className="text-center text-gray-400">
                    <Camera className="w-12 h-12 mx-auto mb-2" />
                    <p>연결 끊김</p>
                  </div>
                </div>
              )}
            </div>
            <div className="flex gap-2 mt-3">
              <Button variant="outline" size="sm" className="flex-1">
                <Maximize2 className="w-4 h-4 mr-2" />
                전체화면
              </Button>
              <Button variant="outline" size="sm" className="flex-1">
                <RotateCw className="w-4 h-4 mr-2" />
                새로고침
              </Button>
            </div>
            <div className="mt-3 p-3 bg-linear-to-r from-blue-50 to-purple-50 rounded-lg border border-purple-200">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">주인 음성 전달</span>
                <Mic className="w-4 h-4 text-purple-600" />
              </div>
              <Button 
                onClick={handleSendOwnerVoice}
                disabled={sendingVoice}
                className="w-full bg-linear-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              >
                <Send className="w-4 h-4 mr-2" />
                {sendingVoice ? "전송 중..." : "음성 전송하기"}
              </Button>
              <p className="text-xs text-gray-600 mt-2">
                반려동물을 안정시키기 위해 음성을 전달합니다
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Quick Stats */}
        <Card>
          <CardHeader>
            <CardTitle>빠른 상태 확인</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                <div className="text-sm text-gray-600 mb-1">CO₂ 상태</div>
                <div className="text-xl font-bold text-green-700">
                  {co2Status.status}
                </div>
              </div>
              <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                <div className="text-sm text-gray-600 mb-1">온도 상태</div>
                <div className="text-xl font-bold text-green-700">
                  {tempStatus.status}
                </div>
              </div>
              <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                <div className="text-sm text-gray-600 mb-1">습도 상태</div>
                <div className="text-xl font-bold text-green-700">
                  {humidityStatus.status}
                </div>
              </div>
              <div className="p-4 bg-purple-50 border border-purple-200 rounded-lg">
                <div className="text-sm text-gray-600 mb-1">CCTV</div>
                <div className="text-xl font-bold text-purple-700">
                  {cctvConnected ? '정상' : '오류'}
                </div>
              </div>
            </div>
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="text-sm text-gray-600 mb-2">오늘 활동 요약</div>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">급식 횟수</span>
                  <span className="font-semibold">{stats ? `${stats.foodSupplyCount}회 (${stats.foodSupplyAmount}g)` : '0회 (0g)'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">급수 횟수</span>
                  <span className="font-semibold">{stats ? `${stats.waterSupplyCount}회 (${stats.waterSupplyAmount}ml)` : '0회 (0ml)'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">환풍 작동</span>
                  <span className="font-semibold">{stats ? `${stats.fanRunCount}회` : '0회'}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Real-time Data Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* CO2 Card */}
        <Card className={`border-2 ${co2Status.border}`}>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              이산화탄소 (CO₂)
            </CardTitle>
            <WindIcon className={`w-5 h-5 ${co2Status.text}`} />
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-baseline gap-2">
                <span className="text-4xl font-bold text-gray-900">
                  {formatSensorValue(currentData.co2, 0)}
                </span>
                <span className="text-lg text-gray-500">ppm</span>
              </div>
              <Badge className={`${co2Status.bg} ${co2Status.text} border ${co2Status.border}`}>
                {co2Status.status}
              </Badge>
              <div className="pt-2 text-xs text-gray-500">
                권장 범위: 400-500 ppm
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Temperature Card */}
        <Card className={`border-2 ${tempStatus.border}`}>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              온도
            </CardTitle>
            <Thermometer className={`w-5 h-5 ${tempStatus.text}`} />
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-baseline gap-2">
                <span className="text-4xl font-bold text-gray-900">
                  {formatSensorValue(currentData.temperature, 1)}
                </span>
                <span className="text-lg text-gray-500">°C</span>
              </div>
              <Badge className={`${tempStatus.bg} ${tempStatus.text} border ${tempStatus.border}`}>
                {tempStatus.status}
              </Badge>
              <div className="pt-2 text-xs text-gray-500">
                권장 범위: 20-24°C
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Humidity Card */}
        <Card className={`border-2 ${humidityStatus.border}`}>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              습도
            </CardTitle>
            <Droplets className={`w-5 h-5 ${humidityStatus.text}`} />
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-baseline gap-2">
                <span className="text-4xl font-bold text-gray-900">
                  {formatSensorValue(currentData.humidity, 0)}
                </span>
                <span className="text-lg text-gray-500">%</span>
              </div>
              <Badge className={`${humidityStatus.bg} ${humidityStatus.text} border ${humidityStatus.border}`}>
                {humidityStatus.status}
              </Badge>
              <div className="pt-2 text-xs text-gray-500">
                권장 범위: 40-65%
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <CardTitle>최근 활동</CardTitle>
          <select
            value={timeFilter}
            onChange={(e) => setTimeFilter(e.target.value as any)}
            className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500 cursor-pointer shadow-xs transition-all hover:border-slate-300"
          >
            <option value="1h">1시간 전</option>
            <option value="24h">하루 전</option>
            <option value="1m">한달 전</option>
            <option value="3m">3달 전</option>
            <option value="all">전체 정보</option>
          </select>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {paginatedActivities.length > 0 ? (
              paginatedActivities.map((activity, index) => {
                const date = new Date(activity.timestamp);
                const timeStr = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
                const isFan = activity.type === 'FAN';
                
                return (
                  <div key={index} className="flex items-start gap-4 pb-4 border-b border-gray-100 last:border-0 last:pb-0">
                    <div className="text-sm font-medium text-gray-500 w-12">{timeStr}</div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-gray-900">{activity.message}</span>
                        <Badge variant={isFan ? 'secondary' : 'default'} className="text-xs">
                          {isFan ? '정보' : '완료'}
                        </Badge>
                      </div>
                      {activity.details && (
                        <div className="text-xs text-gray-500 mt-1 pl-0.5">{activity.details}</div>
                      )}
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="text-center py-6 text-gray-400">최근 활동 내역이 없습니다.</div>
            )}

            {filteredActivities.length > itemsPerPage && (
              <div className="flex items-center justify-between pt-4 border-t border-gray-100 mt-4">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                  disabled={currentPage === 1}
                  className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-50 transition-all cursor-pointer"
                >
                  이전
                </Button>
                <span className="text-xs font-semibold text-slate-500">
                  페이지 {currentPage} / {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                  disabled={currentPage === totalPages}
                  className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-50 transition-all cursor-pointer"
                >
                  다음
                </Button>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}