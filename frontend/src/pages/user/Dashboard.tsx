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
import { getLatestSensorData } from "../../services/dashboardApi";
import { usePetHouse } from "../../store/petStore";

export function Dashboard() {
  const { activeHouse } = usePetHouse();

  const [currentData, setCurrentData] = useState({
    co2: 450,
    temperature: 22.5,
    humidity: 55,
    petPresent: true,
  });

  const [cctvConnected] = useState(true);
  const [sendingVoice, setSendingVoice] = useState(false);

  useEffect(() => {
    const fetchSensorData = async () => {
      try {
        const data = await getLatestSensorData(activeHouse.id);
        setCurrentData(prev => ({
          ...prev,
          co2: data.co2 ?? prev.co2,
          temperature: data.temperature ?? prev.temperature,
          humidity: data.humidity ?? prev.humidity,
        }));
      } catch (error) {
        console.error('[Dashboard] API Fetch Error:', error);
      }
    };

    fetchSensorData();

    const interval = setInterval(fetchSensorData, 3000);

    return () => clearInterval(interval);
  }, [activeHouse.id]);

  const handleSendOwnerVoice = () => {
    setSendingVoice(true);
    toast.success("주인 음성 전송 중...");
    setTimeout(() => {
      setSendingVoice(false);
      toast.success("음성이 펫하우스로 전송되었습니다");
    }, 2000);
  };

  const getStatusColor = (value: number, type: string) => {
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
                  <span className="text-gray-600">���식 횟수</span>
                  <span className="font-semibold">2회</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">급수 횟수</span>
                  <span className="font-semibold">3회</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">짖음 감지</span>
                  <span className="font-semibold text-orange-600">5회</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">환풍 작동</span>
                  <span className="font-semibold">4회</span>
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
                  {currentData.co2.toFixed(0)}
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
                  {currentData.temperature.toFixed(1)}
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
                  {currentData.humidity.toFixed(0)}
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
        <CardHeader>
          <CardTitle>최근 활동</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[
              { time: "14:23", event: "급수 완료", detail: "200ml 공급됨", type: "success" },
              { time: "13:45", event: "짖음 감지", detail: "3회 연속", type: "warning" },
              { time: "12:00", event: "급식 완료", detail: "100g 공급됨", type: "success" },
              { time: "11:30", event: "환풍 작동", detail: "10분간 가동", type: "info" },
              { time: "10:15", event: "CO₂ 농도 상승", detail: "580ppm 도달", type: "warning" },
            ].map((activity, index) => (
              <div key={index} className="flex items-start gap-4 pb-4 border-b border-gray-100 last:border-0 last:pb-0">
                <div className="text-sm font-medium text-gray-500 w-12">{activity.time}</div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">{activity.event}</span>
                    <Badge variant={
                      activity.type === 'success' ? 'default' :
                      activity.type === 'warning' ? 'destructive' : 'secondary'
                    } className="text-xs">
                      {activity.type === 'success' ? '완료' :
                       activity.type === 'warning' ? '주의' : '정보'}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">{activity.detail}</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}