import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Slider } from "../../components/ui/slider";
import { 
  Utensils, 
  Droplets, 
  Plus,
  Trash2,
  Clock,
  Calendar,
  Edit,
  Play
} from "lucide-react";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { usePetHouse } from "../../store/petStore";
import * as supplyApi from "../../services/supplyApi";
import type { SupplyScheduleResponse, SupplyLogHistoryResponse } from "../../types/api";

interface Schedule {
  id: string;
  type: 'feed' | 'water';
  time: string;
  amount: number;
  enabled: boolean;
}

interface History {
  id: string;
  type: 'feed' | 'water';
  timestamp: string;
  amount: number;
  mode: 'manual' | 'auto';
}

/** 백엔드 SupplyScheduleResponse → 프론트 Schedule 변환 */
function toSchedule(res: SupplyScheduleResponse): Schedule {
  // cronExpression에서 시간 추출 시도 (e.g. "0 0 8 * * ?" → "08:00")
  let time = '00:00';
  try {
    const parts = res.cronExpression.split(' ');
    if (parts.length >= 3) {
      time = `${parts[2].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
    }
  } catch { /* mock time 유지 */ }

  return {
    id: String(res.scheduleId),
    type: res.feedType === 'FOOD' ? 'feed' : 'water',
    time,
    amount: res.amount,
    enabled: res.enabled,
  };
}

/** 백엔드 SupplyLogHistoryResponse → 프론트 History 변환 */
function toHistory(res: SupplyLogHistoryResponse, index: number): History {
  return {
    id: String(index),
    type: res.feedType === 'FOOD' ? 'feed' : 'water',
    timestamp: res.createdAt,
    amount: res.amount,
    mode: res.triggerType === 'AUTO' ? 'auto' : 'manual',
  };
}


export function FeedWater() {
  const { activeHouse } = usePetHouse();
  const [feedAmount, setFeedAmount] = useState(100);
  const [waterAmount, setWaterAmount] = useState(200);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [history, setHistory] = useState<History[]>([]);
  const [timeFilter, setTimeFilter] = useState<'1h' | '24h' | '1m' | '3m' | 'all'>('all');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  useEffect(() => {
    setCurrentPage(1);
  }, [timeFilter, activeHouse?.id]);

  const getFilteredHistory = () => {
    if (timeFilter === 'all') return history;

    const now = new Date();
    return history.filter((item) => {
      const itemDate = new Date(item.timestamp);
      const diffMs = now.getTime() - itemDate.getTime();
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

  const filteredHistory = getFilteredHistory();
  const totalPages = Math.max(1, Math.ceil(filteredHistory.length / itemsPerPage));
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedHistory = filteredHistory.slice(startIndex, startIndex + itemsPerPage);
  
  const fetchData = async () => {
    if (!activeHouse?.id) return;
    try {
      const [schedulePage, historyPage] = await Promise.all([
        supplyApi.getSupplySchedules(activeHouse.id),
        supplyApi.getSupplyHistory(activeHouse.id, 0, 200),
      ]);
      setSchedules(schedulePage.content.map(toSchedule));
      setHistory(historyPage.content.map(toHistory));
    } catch (error) {
      console.error('[FeedWater] API Fetch Error:', error);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeHouse?.id]);

  const [newSchedule, setNewSchedule] = useState({
    type: 'feed' as 'feed' | 'water',
    time: '12:00',
    amount: 100,
  });

  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);

  const handleManualFeed = async (amount: number) => {
    if (!activeHouse?.id) return;
    try {
      console.log('[FeedWater] 급여 요청 시작:', { amount, houseId: activeHouse.id });
      const response = await supplyApi.recordSupplyLog(activeHouse.id, {
        scheduleId: null,
        feedType: 'FOOD',
        unitType: 'g',
        amount: amount,
        triggerType: 'MANUAL'
      });
      console.log('[FeedWater] 급여 요청 완료:', response);
      toast.success(`사료 ${amount}g이 공급되었습니다`);
      // 약간의 딜레이 후 데이터 갱신 (MQTT 처리 완료 대기)
      setTimeout(() => {
        fetchData();
      }, 500);
    } catch (error) {
      console.error('[FeedWater] 급여 요청 실패:', error);
      toast.error("사료 공급에 실패했습니다");
    }
  };

  const handleManualWater = async (amount: number) => {
    if (!activeHouse?.id) return;
    try {
      console.log('[FeedWater] 급수 요청 시작:', { amount, houseId: activeHouse.id });
      const response = await supplyApi.recordSupplyLog(activeHouse.id, {
        scheduleId: null,
        feedType: 'WATER',
        unitType: 'ml',
        amount: amount,
        triggerType: 'MANUAL'
      });
      console.log('[FeedWater] 급수 요청 완료:', response);
      toast.success(`물 ${amount}ml가 공급되었습니다`);
      // 약간의 딜레이 후 데이터 갱신 (MQTT 처리 완료 대기)
      setTimeout(() => {
        fetchData();
      }, 500);
    } catch (error) {
      console.error('[FeedWater] 급수 요청 실패:', error);
      toast.error("물 공급에 실패했습니다");
    }
  };

  const handleAddSchedule = async () => {
    if (!activeHouse?.id) return;
    try {
      const [hours, minutes] = newSchedule.time.split(':');
      const cronExpression = `0 ${minutes} ${hours} * * ?`;
      
      await supplyApi.createSupplySchedule(activeHouse.id, {
        feedType: newSchedule.type === 'feed' ? 'FOOD' : 'WATER',
        unitType: newSchedule.type === 'feed' ? 'g' : 'ml',
        amount: newSchedule.amount,
        cronExpression,
        enabled: true,
      });
      toast.success("스케줄이 추가되었습니다");
      fetchData();
    } catch (error) {
      toast.error("스케줄 추가 실패");
    }
  };

  const handleDeleteSchedule = async (id: string) => {
    if (!activeHouse?.id) return;
    try {
      await supplyApi.deleteSupplySchedule(activeHouse.id, Number(id));
      toast.success("스케줄이 삭제되었습니다");
      fetchData();
    } catch (error) {
      toast.error("스케줄 삭제 실패");
    }
  };

  const handleEditSchedule = (schedule: Schedule) => {
    setEditingSchedule({ ...schedule });
    setIsEditDialogOpen(true);
  };

  const handleSaveEditSchedule = async () => {
    if (!editingSchedule || !activeHouse?.id) return;
    try {
      const [hours, minutes] = editingSchedule.time.split(':');
      const cronExpression = `0 ${minutes} ${hours} * * ?`;
      
      await supplyApi.updateSupplySchedule(activeHouse.id, Number(editingSchedule.id), {
        feedType: editingSchedule.type === 'feed' ? 'FOOD' : 'WATER',
        unitType: editingSchedule.type === 'feed' ? 'g' : 'ml',
        amount: editingSchedule.amount,
        cronExpression,
        enabled: editingSchedule.enabled,
      });
      setIsEditDialogOpen(false);
      setEditingSchedule(null);
      toast.success("스케줄이 수정되었습니다");
      fetchData();
    } catch (error) {
      toast.error("스케줄 수정 실패");
    }
  };

  const handleToggleSchedule = async (id: string) => {
    if (!activeHouse?.id) return;
    try {
      const target = schedules.find(s => s.id === id);
      if (!target) return;
      await supplyApi.toggleSupplySchedule(activeHouse.id, Number(id), !target.enabled);
      fetchData();
    } catch (error) {
      toast.error("스케줄 상태 변경 실패");
    }
  };

  const formatDateTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return {
      date: date.toLocaleDateString('ko-KR'),
      time: date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' }),
    };
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-3xl font-bold text-gray-900">급여/급수 제어</h2>
        <p className="text-gray-500 mt-2">반려동물의 식사와 물 공급을 관리하세요</p>
      </div>

      {/* Manual Control */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Feed Control */}
        <Card className="border-2 border-orange-200 bg-orange-50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-orange-700">
              <Utensils className="w-5 h-5" />
              사료 공급 (수동)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Slider Control */}
            <div className="space-y-3 p-4 bg-white rounded-lg border border-orange-200">
              <Label className="text-base font-semibold text-gray-900">공급량 조절</Label>
              <div className="flex items-center gap-4">
                <Slider
                  value={[feedAmount]}
                  onValueChange={(value) => setFeedAmount(value[0])}
                  max={200}
                  min={10}
                  step={10}
                  className="flex-1"
                />
                <div className="w-16 text-center">
                  <div className="text-2xl font-bold text-gray-900">{feedAmount}</div>
                  <div className="text-xs text-gray-500">g</div>
                </div>
              </div>
              <Button 
                onClick={() => handleManualFeed(feedAmount)}
                className="w-full bg-orange-500 hover:bg-orange-600"
              >
                {feedAmount}g 공급하기
              </Button>
            </div>
            
            {/* Quick Buttons */}
            <div className="space-y-2">
              <Label className="text-sm text-gray-600">빠른 선택</Label>
              <div className="grid grid-cols-3 gap-2">
                <Button 
                  onClick={() => handleManualFeed(50)}
                  variant="outline"
                  className="border-orange-300 hover:bg-orange-100"
                >
                  50g
                </Button>
                <Button 
                  onClick={() => handleManualFeed(100)}
                  variant="outline"
                  className="border-orange-300 hover:bg-orange-100"
                >
                  100g
                </Button>
                <Button 
                  onClick={() => handleManualFeed(150)}
                  variant="outline"
                  className="border-orange-300 hover:bg-orange-100"
                >
                  150g
                </Button>
              </div>
            </div>
            
            <div className="text-sm text-gray-600 bg-white p-3 rounded border border-orange-200">
              💡 권장 1회 급여량: 100g (체중에 따라 조절)
            </div>
          </CardContent>
        </Card>

        {/* Water Control */}
        <Card className="border-2 border-blue-200 bg-blue-50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-blue-700">
              <Droplets className="w-5 h-5" />
              물 공급 (수동)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Slider Control */}
            <div className="space-y-3 p-4 bg-white rounded-lg border border-blue-200">
              <Label className="text-base font-semibold text-gray-900">공급량 조절</Label>
              <div className="flex items-center gap-4">
                <Slider
                  value={[waterAmount]}
                  onValueChange={(value) => setWaterAmount(value[0])}
                  max={500}
                  min={50}
                  step={50}
                  className="flex-1"
                />
                <div className="w-16 text-center">
                  <div className="text-2xl font-bold text-gray-900">{waterAmount}</div>
                  <div className="text-xs text-gray-500">ml</div>
                </div>
              </div>
              <Button 
                onClick={() => handleManualWater(waterAmount)}
                className="w-full bg-blue-500 hover:bg-blue-600"
              >
                {waterAmount}ml 공급하기
              </Button>
            </div>
            
            {/* Quick Buttons */}
            <div className="space-y-2">
              <Label className="text-sm text-gray-600">빠른 선택</Label>
              <div className="grid grid-cols-3 gap-2">
                <Button 
                  onClick={() => handleManualWater(100)}
                  variant="outline"
                  className="border-blue-300 hover:bg-blue-100"
                >
                  100ml
                </Button>
                <Button 
                  onClick={() => handleManualWater(200)}
                  variant="outline"
                  className="border-blue-300 hover:bg-blue-100"
                >
                  200ml
                </Button>
                <Button 
                  onClick={() => handleManualWater(300)}
                  variant="outline"
                  className="border-blue-300 hover:bg-blue-100"
                >
                  300ml
                </Button>
              </div>
            </div>
            
            <div className="text-sm text-gray-600 bg-white p-3 rounded border border-blue-200">
              💡 권장 1일 급수량: 500-800ml (날씨에 따라 조절)
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Schedule Management */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>자동 공급 스케줄</CardTitle>
          <Dialog>
            <DialogTrigger asChild>
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                스케줄 추가
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>새 스케줄 추가</DialogTitle>
              </DialogHeader>
              <div className="space-y-4 pt-4">
                <div>
                  <Label>종류</Label>
                  <select
                    className="w-full mt-1.5 px-3 py-2 border border-gray-300 rounded-lg"
                    value={newSchedule.type}
                    onChange={(e) => setNewSchedule({ ...newSchedule, type: e.target.value as any })}
                  >
                    <option value="feed">사료</option>
                    <option value="water">물</option>
                  </select>
                </div>
                <div>
                  <Label>시간</Label>
                  <Input
                    type="time"
                    value={newSchedule.time}
                    onChange={(e) => setNewSchedule({ ...newSchedule, time: e.target.value })}
                    className="mt-1.5"
                  />
                </div>
                <div>
                  <Label>용량 ({newSchedule.type === 'feed' ? 'g' : 'ml'})</Label>
                  <Input
                    type="number"
                    value={newSchedule.amount}
                    onChange={(e) => setNewSchedule({ ...newSchedule, amount: Number(e.target.value) })}
                    className="mt-1.5"
                  />
                </div>
                <Button onClick={handleAddSchedule} className="w-full">
                  추가하기
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {schedules.map((schedule) => (
              <div 
                key={schedule.id}
                className={`flex items-center justify-between p-4 rounded-lg border-2 transition-all ${
                  schedule.enabled 
                    ? schedule.type === 'feed' 
                      ? 'bg-orange-50 border-orange-200' 
                      : 'bg-blue-50 border-blue-200'
                    : 'bg-gray-50 border-gray-200 opacity-60'
                }`}
              >
                <div className="flex items-center gap-4">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                    schedule.type === 'feed' ? 'bg-orange-500' : 'bg-blue-500'
                  }`}>
                    {schedule.type === 'feed' ? (
                      <Utensils className="w-5 h-5 text-white" />
                    ) : (
                      <Droplets className="w-5 h-5 text-white" />
                    )}
                  </div>
                  <div>
                    <div className="font-medium text-gray-900">
                      {schedule.type === 'feed' ? '사료 공급' : '물 공급'}
                    </div>
                    <div className="text-sm text-gray-600 flex items-center gap-2 mt-1">
                      <Clock className="w-3 h-3" />
                      {schedule.time} - {schedule.amount}{schedule.type === 'feed' ? 'g' : 'ml'}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={schedule.enabled ? 'default' : 'secondary'}>
                    {schedule.enabled ? '활성' : '비활성'}
                  </Badge>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleEditSchedule(schedule)}
                    title="스케줄 수정"
                  >
                    <Edit className="w-4 h-4 text-blue-500" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleToggleSchedule(schedule.id)}
                  >
                    <Play className="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => handleDeleteSchedule(schedule.id)}
                  >
                    <Trash2 className="w-4 h-4 text-red-500" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Edit Schedule Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>스케줄 수정</DialogTitle>
          </DialogHeader>
          {editingSchedule && (
            <div className="space-y-4 pt-4">
              <div>
                <Label>종류</Label>
                <select
                  className="w-full mt-1.5 px-3 py-2 border border-gray-300 rounded-lg"
                  value={editingSchedule.type}
                  onChange={(e) => setEditingSchedule({ ...editingSchedule, type: e.target.value as 'feed' | 'water' })}
                >
                  <option value="feed">사료</option>
                  <option value="water">물</option>
                </select>
              </div>
              <div>
                <Label>시간</Label>
                <Input
                  type="time"
                  value={editingSchedule.time}
                  onChange={(e) => setEditingSchedule({ ...editingSchedule, time: e.target.value })}
                  className="mt-1.5"
                />
              </div>
              <div>
                <Label>용량 ({editingSchedule.type === 'feed' ? 'g' : 'ml'})</Label>
                <Input
                  type="number"
                  value={editingSchedule.amount}
                  onChange={(e) => setEditingSchedule({ ...editingSchedule, amount: Number(e.target.value) })}
                  className="mt-1.5"
                />
              </div>
              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>
                  취소
                </Button>
                <Button className="flex-1" onClick={handleSaveEditSchedule}>
                  저장하기
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* History */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <CardTitle>급여/급수 이력</CardTitle>
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
          <div className="space-y-3">
            {paginatedHistory.length > 0 ? (
              paginatedHistory.map((item) => {
                const { date, time } = formatDateTime(item.timestamp);
                return (
                  <div 
                    key={item.id}
                    className="flex items-center gap-4 p-3 bg-gray-50 rounded-lg border border-gray-200"
                  >
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
                      item.type === 'feed' ? 'bg-orange-100' : 'bg-blue-100'
                    }`}>
                      {item.type === 'feed' ? (
                        <Utensils className="w-4 h-4 text-orange-600" />
                      ) : (
                        <Droplets className="w-4 h-4 text-blue-600" />
                      )}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-gray-900">
                          {item.type === 'feed' ? '사료' : '물'} {item.amount}{item.type === 'feed' ? 'g' : 'ml'}
                        </span>
                        <Badge variant={item.mode === 'auto' ? 'default' : 'secondary'} className="text-xs">
                          {item.mode === 'auto' ? '자동' : '수동'}
                        </Badge>
                      </div>
                      <div className="text-sm text-gray-500 mt-1 flex items-center gap-2">
                        <Calendar className="w-3 h-3" />
                        {date} {time}
                      </div>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="text-center py-6 text-gray-400">급여/급수 내역이 없습니다.</div>
            )}

            {filteredHistory.length > itemsPerPage && (
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