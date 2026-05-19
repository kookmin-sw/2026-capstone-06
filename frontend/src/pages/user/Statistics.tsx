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
  BarChart,
  Bar,
  Cell
} from "recharts";

export function Statistics() {
  const [period, setPeriod] = useState<'daily' | 'weekly' | 'monthly'>('daily');
  const [data, setData] = useState<any[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}/stats?period=${period}`);
        const result = await response.json();
        setData(result);
      } catch (error) {
        console.error('[Stats] Fetch Error:', error);
      }
    };
    fetchData();
  }, [period]);

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

                    {/* Barking Chart */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                🐾 짖음 횟수 추이
              </CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={data} barCategoryGap="30%">
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} />
                  <XAxis
                    dataKey="time"
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                  />
                  <YAxis
                    stroke="#6b7280"
                    style={{ fontSize: '12px' }}
                    allowDecimals={false}
                    domain={[0, 'auto']}
                    label={{ value: '횟수', angle: -90, position: 'insideLeft', offset: 10, style: { fontSize: '12px', fill: '#6b7280' } }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: 'white',
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '12px'
                    }}
                    formatter={(value: any) => [`${value}회`, '짖음 횟수']}
                  />
                  <Bar dataKey="barking" name="짖음 횟수" radius={[4, 4, 0, 0]}>
                    {data.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={entry.barking >= 6 ? '#ef4444' : entry.barking >= 3 ? '#f97316' : '#f59e0b'}
                      />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
              <div className="mt-4 flex items-center gap-6 text-sm text-gray-500">
                <span>합계: <span className="font-semibold text-gray-800">{data.reduce((acc, d) => acc + (d.barking ?? 0), 0)}회</span></span>
                <span>평균: <span className="font-semibold text-gray-800">{(data.reduce((acc, d) => acc + (d.barking ?? 0), 0) / data.length).toFixed(1)}회</span></span>
                <div className="flex items-center gap-3 ml-auto">
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-sm inline-block bg-amber-400"></span> 낮음 (0~2)</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-sm inline-block bg-orange-400"></span> 보통 (3~5)</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-sm inline-block bg-red-500"></span> 높음 (6+)</span>
                </div>
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
