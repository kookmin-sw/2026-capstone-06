import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../../components/ui/tabs";
import { 
  LineChart, 
  Line, 
  AreaChart,
  Area,
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer ,
} from "recharts";
import { usePetHouse } from "../../store/petStore";
import * as sensorApi from "../../services/sensorApi";

export function Statistics() {
  const { activeHouse } = usePetHouse();
  const [period, setPeriod] = useState<'daily' | 'weekly' | 'monthly'>('daily');
  const [data, setData] = useState<any[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        let range = '-24h';
        let interval = '1h';
        
        if (period === 'weekly') {
          range = '-7d';
          interval = '6h';
        } else if (period === 'monthly') {
          range = '-30d';
          interval = '1d';
        }

        // petStore에 serialNum이 있으면 사용 (로그인 후 loadDevicesFromServer 호출 결과)
        // 없으면 activeHouse.id로 폴백 (개발 환경 또는 더미 데이터)
        if (!activeHouse?.id) return;
        const serialNum = activeHouse.serialNum ?? activeHouse.id;
        
        const response = await sensorApi.getChartData(serialNum, range, interval);
        
        // 데이터 매핑: SensorResponse -> 차트 데이터
        const mapped = response.map(item => {
          let timeLabel = item.regDate;
          if (item.regDate && item.regDate.length >= 14) {
            // "yyyyMMddHHmmss" 형식 파싱
            const m = item.regDate.substring(4, 6);
            const d = item.regDate.substring(6, 8);
            const h = item.regDate.substring(8, 10);
            
            if (period === 'daily') {
              timeLabel = `${h}:00`;
            } else {
              timeLabel = `${m}/${d} ${h}:00`;
            }
          }
          
          return {
            time: timeLabel,
            co2: Math.round(item.coVal || 0),
            temperature: Number((item.temVal || 0).toFixed(1)),
            humidity: Math.round(item.humVal || 0)
          };
        });
        
        setData(mapped);
      } catch (error) {
        console.error('[Stats] Fetch Error:', error);
      }
    };
    fetchData();
  }, [period, activeHouse]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-3xl font-bold text-gray-900">통계 및 그래프</h2>
        <p className="text-gray-500 mt-2">반려동물 건강 상태 추이를 파악하세요</p>
      </div>

      {/* Period Selector */}
      <Tabs value={period} onValueChange={(v) => setPeriod(v as any)} className="w-full">
        <TabsList className="grid w-full max-w-md grid-cols-3">
          <TabsTrigger value="daily">일간</TabsTrigger>
          <TabsTrigger value="weekly">주간</TabsTrigger>
          <TabsTrigger value="monthly">월간</TabsTrigger>
        </TabsList>

        <TabsContent value={period} className="space-y-6 mt-6">
          {/* CO2 Chart */}
          <Card>
            <CardHeader>
              <CardTitle>이산화탄소 (CO₂) 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={data}>
                  <defs>
                    <linearGradient id="colorCo2" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="time" 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                    domain={[300, 700]}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'white', 
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '12px'
                    }}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="co2" 
                    stroke="#8b5cf6" 
                    fillOpacity={1} 
                    fill="url(#colorCo2)"
                    strokeWidth={2}
                  />
                </AreaChart>
              </ResponsiveContainer>
              <div className="mt-4 text-sm text-gray-500">
                평균: {(data.reduce((acc, d) => acc + d.co2, 0) / data.length).toFixed(0)} ppm
              </div>
            </CardContent>
          </Card>

          {/* Temperature Chart */}
          <Card>
            <CardHeader>
              <CardTitle>온도 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="time" 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                    domain={[15, 30]}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'white', 
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '12px'
                    }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="temperature" 
                    stroke="#ef4444" 
                    strokeWidth={3}
                    dot={{ fill: '#ef4444', r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
              <div className="mt-4 text-sm text-gray-500">
                평균: {(data.reduce((acc, d) => acc + d.temperature, 0) / data.length).toFixed(1)}°C
              </div>
            </CardContent>
          </Card>

          {/* Humidity Chart */}
          <Card>
            <CardHeader>
              <CardTitle>습도 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={data}>
                  <defs>
                    <linearGradient id="colorHumidity" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="time" 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                    domain={[0, 100]}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'white', 
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '12px'
                    }}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="humidity" 
                    stroke="#3b82f6" 
                    fillOpacity={1} 
                    fill="url(#colorHumidity)"
                    strokeWidth={2}
                  />
                </AreaChart>
              </ResponsiveContainer>
              <div className="mt-4 text-sm text-gray-500">
                평균: {(data.reduce((acc, d) => acc + d.humidity, 0) / data.length).toFixed(0)}%
              </div>
            </CardContent>
          </Card>


          {/* Combined Chart */}
          <Card>
            <CardHeader>
              <CardTitle>통합 추이</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={350}>
                <LineChart data={data}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="time" 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis 
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'white', 
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '12px'
                    }}
                  />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="co2" 
                    stroke="#8b5cf6" 
                    name="CO₂ (ppm)"
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="temperature" 
                    stroke="#ef4444" 
                    name="온도 (°C)"
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="humidity" 
                    stroke="#3b82f6" 
                    name="습도 (%)"
                    strokeWidth={2}
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
