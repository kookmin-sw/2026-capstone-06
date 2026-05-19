import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Slider } from "../../components/ui/slider";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { 
  Wind, 
  Power,
  Thermometer,
  Calendar,
  Clock,
  Settings,
  TrendingUp,
  Plus,
  Trash2,
  Play,
  Edit,
  ArrowRight,
  ChevronUp,
  ChevronDown
} from "lucide-react";
import { toast } from "sonner";
import { usePetHouse } from "../../store/petStore";
import * as fanApi from "../../services/fanApi";
import type { FanScheduleResponse } from "../../types/api";

interface VentilationHistory {
  id: string;
  timestamp: string;
  duration: number;
  intensity: number;
  mode: 'manual' | 'auto';
  trigger?: string;
}

interface TempIntensityPair {
  temp: number;
  intensity: number;
}

interface AutoRule {
  id: string;
  timeStart: string;
  timeEnd: string;
  conditions: TempIntensityPair[];
  enabled: boolean;
}

const defaultCondition: TempIntensityPair = { temp: 25, intensity: 70 };

const defaultNewRule = {
  timeStart: '09:00',
  timeEnd: '21:00',
  conditions: [{ temp: 25, intensity: 70 }] as TempIntensityPair[],
};

/** 백엔드 FanScheduleResponse → 프론트 AutoRule 변환 */
function toAutoRule(res: FanScheduleResponse): AutoRule {
  return {
    id: String(res.scheduleId),
    timeStart: res.startTime?.substring(0, 5) ?? '00:00',  // "HH:mm:ss" → "HH:mm"
    timeEnd: res.endTime?.substring(0, 5) ?? '00:00',
    conditions: (res.fanScheduleDetailResponseList ?? []).map(d => ({
      temp: d.temperature,
      intensity: d.speed,
    })),
    enabled: true,  // 백엔드에서 enabled 필드가 별도로 없으므로 기본 true
  };
}

/** 백엔드 FanHistoryResponse → 프론트 VentilationHistory 변환 */
function toVentilationHistory(res: any): VentilationHistory {
  return {
    id: String(res.id),
    timestamp: res.startTime ?? res.createdAt,
    duration: res.durationMinutes ?? 0,
    intensity: res.speed ?? 0,
    mode: res.triggerType?.toLowerCase() === 'auto' ? 'auto' : 'manual',
    trigger: res.executionStatus === 'SUCCESS' ? undefined : '실패',
  };
}


export function Ventilation() {
  const { activeHouse } = usePetHouse();
  const [isRunning, setIsRunning] = useState(false);
  const [intensity, setIntensity] = useState(50);
  const [autoMode, setAutoMode] = useState(false);

  const [autoRules, setAutoRules] = useState<AutoRule[]>([]);
  const [history, setHistory] = useState<VentilationHistory[]>([]);
  const [statistics, setStatistics] = useState<any>(null);

  const fetchSchedules = async () => {
    try {
      if (!activeHouse?.id) return;
      const page = await fanApi.getFanSchedules(activeHouse.id);
      setAutoRules(page.content.map(toAutoRule));
    } catch (error) {
      console.error('[Ventilation] API Fetch Error:', error);
    }
  };

  const fetchHistoryAndStatistics = async () => {
    try {
      if (!activeHouse?.id) return;
      const stats = await fanApi.getFanStatistics(activeHouse.id);
      setStatistics(stats);

      const histPage = await fanApi.getFanHistory(activeHouse.id, 0, 10);
      setHistory(histPage.content.map(toVentilationHistory));
    } catch (error) {
      console.error('[Ventilation] Fetch History/Stats Error:', error);
    }
  };

  // API에서 환풍기 데이터 가져오기
  useEffect(() => {
    fetchSchedules();
    fetchHistoryAndStatistics();
  }, [activeHouse.id]);

  const [newRule, setNewRule] = useState({ ...defaultNewRule, conditions: [{ ...defaultCondition }] });
  const [editingRule, setEditingRule] = useState<AutoRule | null>(null);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [expandedRules, setExpandedRules] = useState<Set<string>>(new Set(['1']));
  const [addDialogOpen, setAddDialogOpen] = useState(false);


  const handleToggleVentilation = async () => {
    const nextState = !isRunning;
    try {
      await fanApi.controlFan(activeHouse.id, {
        isRunning: nextState,
        intensity: intensity
      });
      setIsRunning(nextState);
      if (nextState) {
        toast.success(`환풍기 작동 시작 (강도: ${intensity}%)`);
        if (autoMode) {
          setAutoMode(false);
          toast.info("수동 제어 시작으로 자동 모드가 비활성화되었습니다");
        }
      } else {
        toast.info("환풍기 중지");
      }
      await fetchHistoryAndStatistics();
    } catch (error) {
      console.error(error);
      toast.error("환풍기 제어에 실패했습니다");
    }
  };

  const handleIntensityChange = async (value: number[]) => {
    const newIntensity = value[0];
    setIntensity(newIntensity);
    if (isRunning) {
      try {
        await fanApi.controlFan(activeHouse.id, {
          isRunning: true,
          intensity: newIntensity
        });
        toast.info(`강도 변경: ${newIntensity}%`);
        await fetchHistoryAndStatistics();
      } catch (error) {
        console.error(error);
        toast.error("강도 변경에 실패했습니다");
      }
    }
  };

    const handleAutoVentilation = async () => {
    const nextMode = !autoMode;
    try {
      await fanApi.toggleFanAutoMode(activeHouse.id, nextMode);
      setAutoMode(nextMode);
      if (nextMode) {
        toast.success("자동 환풍기 가동");
        if (isRunning) {
          setIsRunning(false);
          toast.info("자동 제어 시작으로 수동 모드가 비활성되었습니다");
        }
      } else {
        toast.info("자동 환풍기 중지");
      }
      await fetchHistoryAndStatistics();
    } catch (error) {
      console.error(error);
      toast.error("자동 모드 설정에 실패했습니다");
    }
  }

    // ── New Rule Condition Helpers ──────────────────────────────────────────────
  const handleAddCondition = () => {
    const last = newRule.conditions[newRule.conditions.length - 1];
    setNewRule({
      ...newRule,
      conditions: [...newRule.conditions, { temp: last.temp + 3, intensity: Math.min(100, last.intensity + 10) }],
    });
  };

  const handleRemoveCondition = (index: number) => {
    if (newRule.conditions.length === 1) return;
    const updated = newRule.conditions.filter((_, i) => i !== index);
    setNewRule({ ...newRule, conditions: updated });
  };

  const handleUpdateCondition = (index: number, field: keyof TempIntensityPair, value: number) => {
    const updated = newRule.conditions.map((c, i) =>
      i === index ? { ...c, [field]: field === 'intensity' ? Math.min(100, Math.max(1, value)) : value } : c
    );
    setNewRule({ ...newRule, conditions: updated });
  };

  const handleAddRule = async () => {
    try {
      const sorted = [...newRule.conditions].sort((a, b) => a.temp - b.temp);
      const request = {
        startTime: `${newRule.timeStart}:00`,
        endTime: `${newRule.timeEnd}:00`,
        enabled: true,
        fanScheduleDetailRequestList: sorted.map(c => ({
          temperature: c.temp,
          speed: c.intensity
        }))
      };
      await fanApi.createFanSchedule(activeHouse.id, request);
      setNewRule({ timeStart: '09:00', timeEnd: '21:00', conditions: [{ ...defaultCondition }] });
      setAddDialogOpen(false);
      await fetchSchedules();
      await fetchHistoryAndStatistics();
      toast.success("규칙이 추가되었습니다");
    } catch (error) {
      console.error(error);
      toast.error("규칙 추가에 실패했습니다");
    }
  };


  // ── Edit Rule Condition Helpers ─────────────────────────────────────────────
  const handleAddEditCondition = () => {
    if (!editingRule) return;
    const last = editingRule.conditions[editingRule.conditions.length - 1];
    setEditingRule({
      ...editingRule,
      conditions: [...editingRule.conditions, { temp: last.temp + 3, intensity: Math.min(100, last.intensity + 10) }],
    });
  };

  const handleRemoveEditCondition = (index: number) => {
    if (!editingRule || editingRule.conditions.length === 1) return;
    setEditingRule({
      ...editingRule,
      conditions: editingRule.conditions.filter((_, i) => i !== index),
    });
  };

  const handleUpdateEditCondition = (index: number, field: keyof TempIntensityPair, value: number) => {
    if (!editingRule) return;
    const updated = editingRule.conditions.map((c, i) =>
      i === index ? { ...c, [field]: field === 'intensity' ? Math.min(100, Math.max(1, value)) : value } : c
    );
    setEditingRule({ ...editingRule, conditions: updated });
  };

  const handleDeleteRule = async (id: string) => {
    try {
      await fanApi.deleteFanSchedule(activeHouse.id, Number(id));
      await fetchSchedules();
      await fetchHistoryAndStatistics();
      toast.success("규칙이 삭제되었습니다");
    } catch (error) {
      console.error(error);
      toast.error("규칙 삭제에 실패했습니다");
    }
  };

  const handleToggleRule = async (id: string) => {
    try {
      const rule = autoRules.find(r => r.id === id);
      if (!rule) return;
      await fanApi.toggleFanSchedule(activeHouse.id, Number(id), !rule.enabled);
      await fetchSchedules();
      await fetchHistoryAndStatistics();
    } catch (error) {
      console.error(error);
      toast.error("규칙 상태 변경에 실패했습니다");
    }
  };

  const handleEditRule = (rule: AutoRule) => {
    setEditingRule({ ...rule, conditions: rule.conditions.map(c => ({ ...c })) });
    setIsEditDialogOpen(true);
  };

  const handleSaveEditRule = async () => {
    if (!editingRule) return;
    try {
      const sorted = [...editingRule.conditions].sort((a, b) => a.temp - b.temp);
      const request = {
        startTime: `${editingRule.timeStart}:00`,
        endTime: `${editingRule.timeEnd}:00`,
        enabled: editingRule.enabled,
        fanScheduleDetailRequestList: sorted.map(c => ({
          temperature: c.temp,
          speed: c.intensity
        }))
      };
      await fanApi.updateFanSchedule(activeHouse.id, Number(editingRule.id), request);
      setIsEditDialogOpen(false);
      setEditingRule(null);
      await fetchSchedules();
      await fetchHistoryAndStatistics();
      toast.success("규칙이 수정되었습니다");
    } catch (error) {
      console.error(error);
      toast.error("규칙 수정에 실패했습니다");
    }
  };

  const toggleExpand = (id: string) => {
    setExpandedRules(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const formatDateTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return {
      date: date.toLocaleDateString('ko-KR'),
      time: date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' }),
    };
  };

    // ── Reusable Condition Editor ───────────────────────────────────────────────
  const ConditionEditor = ({
    conditions,
    onAdd,
    onRemove,
    onUpdate,
  }: {
    conditions: TempIntensityPair[];
    onAdd: () => void;
    onRemove: (i: number) => void;
    onUpdate: (i: number, field: keyof TempIntensityPair, value: number) => void;
  }) => (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <Label>기준 온도 / 환풍기 강도 조건</Label>
        <Button type="button" variant="outline" size="sm" onClick={onAdd} className="h-7 text-xs gap-1">
          <Plus className="w-3 h-3" />
          조건 추가
        </Button>
      </div>

      {/* Header row */}
      <div className="grid grid-cols-[1fr_16px_1fr_32px] gap-2 px-1">
        <span className="text-xs text-gray-500 text-center">기준 온도 (°C 이상)</span>
        <span />
        <span className="text-xs text-gray-500 text-center">환풍기 강도 (%)</span>
        <span />
      </div>

      <div className="space-y-2 max-h-52 overflow-y-auto pr-1">
        {conditions.map((cond, i) => (
          <div key={i} className="grid grid-cols-[1fr_16px_1fr_32px] items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
            <div className="flex items-center gap-1">
              <Thermometer className="w-3.5 h-3.5 text-red-400 shrink-0" />
              <Input
                type="number"
                value={cond.temp}
                onChange={(e) => onUpdate(i, 'temp', Number(e.target.value))}
                className="h-8 text-sm"
              />
            </div>
            <ArrowRight className="w-4 h-4 text-gray-400" />
            <div className="flex items-center gap-1">
              <Wind className="w-3.5 h-3.5 text-blue-400 shrink-0" />
              <Input
                type="number"
                min={1}
                max={100}
                value={cond.intensity}
                onChange={(e) => onUpdate(i, 'intensity', Number(e.target.value))}
                className="h-8 text-sm"
              />
            </div>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-red-400 hover:text-red-600 hover:bg-red-50"
              onClick={() => onRemove(i)}
              disabled={conditions.length === 1}
            >
              <Trash2 className="w-3.5 h-3.5" />
            </Button>
          </div>
        ))}
      </div>

      {conditions.length > 1 && (
        <p className="text-xs text-gray-400">💡 저장 시 기준 온도 오름차순으로 자동 정렬됩니다.</p>
      )}
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-3xl font-bold text-gray-900">환풍기 제어</h2>
        <p className="text-gray-500 mt-2">펫하우스 내부 공기 순환을 관리하세요</p>
      </div>

      {/* Manual Control */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Control Panel */}
        <Card className={`border-2 transition-all ${
          isRunning 
            ? 'border-green-300 bg-green-50' 
            : 'border-gray-200 bg-white'
        }`}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Wind className={`w-5 h-5 ${isRunning ? 'text-green-600 animate-spin' : 'text-gray-600'}`} />
              수동 제어
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Power Button */}
            <div className="flex items-center justify-between p-4 bg-white rounded-lg border border-gray-200">
              <div className="flex items-center gap-3">
                <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                  isRunning ? 'bg-green-500' : 'bg-gray-300'
                }`}>
                  <Power className="w-6 h-6 text-white" />
                </div>
                <div>
                  <div className="font-semibold text-gray-900">환풍기 전원</div>
                  <div className="text-sm text-gray-500">
                    {isRunning ? '작동 중' : '정지됨'}
                  </div>
                </div>
              </div>
              <Button
                onClick={handleToggleVentilation}
                className={isRunning ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'}
              >
                {isRunning ? '중지' : '시작'}
              </Button>
            </div>

            {/* Intensity Control */}
            <div className="space-y-4 p-4 bg-white rounded-lg border border-gray-200">
              <Label className="text-base font-semibold">강도 조절</Label>
              <div className="flex items-center gap-4">
                <Slider
                  value={[intensity]}
                  onValueChange={handleIntensityChange}
                  max={100}
                  step={10}
                  className="flex-1"
                  disabled={!isRunning}
                />
                <div className="w-16 text-center">
                  <div className="text-2xl font-bold text-gray-900">{intensity}</div>
                  <div className="text-xs text-gray-500">%</div>
                </div>
              </div>
              <div className="grid grid-cols-3 gap-2 mt-2">
                <Button variant="outline" size="sm" onClick={() => handleIntensityChange([30])} disabled={!isRunning}>약</Button>
                <Button variant="outline" size="sm" onClick={() => handleIntensityChange([60])} disabled={!isRunning}>중</Button>
                <Button variant="outline" size="sm" onClick={() => handleIntensityChange([90])} disabled={!isRunning}>강</Button>
              </div>
            </div>

            {/* Current Status */}
            {isRunning && (
              <div className="p-4 bg-green-100 border border-green-300 rounded-lg">
                <div className="flex items-center gap-2 text-green-700">
                  <Wind className="w-5 h-5 animate-spin" />
                  <span className="font-medium">환풍 작동 중 ({intensity}% 강도)</span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Auto Mode Settings */}
        <Card className={`border-2 transition-all ${
          autoMode ? 'border-blue-200 bg-blue-50' : 'border-gray-200 bg-gray-50'
        }`}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-gray-600">
              <Settings className="w-5 h-5" />
              자동 제어 설정
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Auto Mode Toggle */}
            <div className={`flex items-center justify-between p-4 rounded-lg border transition-all ${
              autoMode ? 'bg-white border-blue-200' : 'bg-gray-100 border-gray-200'
            }`}>
              <div>
                <div className="font-semibold">자동 모드</div>
                <div className="text-sm mt-1">
                  시간대별 온도 규칙에 따라 자동으로 작동
                </div>
              </div>
              <Button
                onClick={handleAutoVentilation}
                className={autoMode ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'}
              >
                {autoMode ? '중지' : '시작'}
              </Button>
            </div>


            <div className={`p-4 rounded-lg border transition-all ${
              autoMode ? 'bg-white border-blue-200' : 'bg-gray-100 border-gray-200 opacity-50'
            }`}>
              <div className={`text-sm font-medium mb-3 ${autoMode ? 'text-gray-700' : 'text-gray-400'}`}>활성 규칙 요약</div>
              {autoRules.filter(r => r.enabled).length === 0 ? (
                <div className={`text-sm ${autoMode ? 'text-gray-400' : 'text-gray-300'}`}>활성화된 규칙이 없습니다</div>
              ) : (
                <div className="space-y-3">
                  {autoRules.filter(r => r.enabled).map(r => (
                    <div key={r.id} className="text-sm">
                      <div className={`flex items-center gap-1.5 font-medium mb-1 ${autoMode ? 'text-gray-700' : 'text-gray-400'}`}>
                        <Clock className="w-3 h-3" />
                        {r.timeStart} ~ {r.timeEnd}
                      </div>
                      <div className="pl-4 space-y-0.5">
                        {r.conditions.map((c, i) => (
                          <div key={i} className="flex items-center gap-2">
                            <div className={`w-1.5 h-1.5 rounded-full shrink-0 ${autoMode ? 'bg-blue-400' : 'bg-gray-400'}`} />
                            <span className={autoMode ? 'text-gray-600' : 'text-gray-400'}>
                              {c.temp}°C 이상 → 강도 {c.intensity}%
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {!autoMode && (
              <div className="p-3 bg-gray-200 border border-gray-300 rounded-lg">
                <div className="flex items-center gap-2 text-gray-500 text-sm">
                  <Settings className="w-4 h-4" />
                  <span>자동 모드가 비활성화되어 있습니다. 스위치를 켜면 자동 제어가 시작됩니다.</span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Auto Rules Schedule */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Thermometer className="w-5 h-5 text-blue-600" />
            자동 제어 규칙
          </CardTitle>
          <Dialog open={addDialogOpen} onOpenChange={setAddDialogOpen}>
            <DialogTrigger asChild>
              <Button disabled={!autoMode}>
                <Plus className="w-4 h-4 mr-2" />
                규칙 추가
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>새 자동 제어 규칙 추가</DialogTitle>
              </DialogHeader>
              <div className="space-y-5 pt-4">
                {/* Time Range */}
                <div>
                  <Label className="mb-2 block">적용 시간대</Label>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <Label className="text-xs text-gray-500">시작 시간</Label>
                      <Input
                        type="time"
                        value={newRule.timeStart}
                        onChange={(e) => setNewRule({ ...newRule, timeStart: e.target.value })}
                        className="mt-1"
                      />
                    </div>
                    <div>
                      <Label className="text-xs text-gray-500">종료 시간</Label>
                      <Input
                        type="time"
                        value={newRule.timeEnd}
                        onChange={(e) => setNewRule({ ...newRule, timeEnd: e.target.value })}
                        className="mt-1"
                      />
                    </div>
                  </div>
                </div>

                {/* Conditions */}
                <ConditionEditor
                  conditions={newRule.conditions}
                  onAdd={handleAddCondition}
                  onRemove={handleRemoveCondition}
                  onUpdate={handleUpdateCondition}
                />

                {/* Preview */}
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-800 space-y-1">
                  <div className="font-medium flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    {newRule.timeStart} ~ {newRule.timeEnd} 적용
                  </div>
                  {[...newRule.conditions]
                    .sort((a, b) => a.temp - b.temp)
                    .map((c, i) => (
                      <div key={i} className="pl-4 flex items-center gap-1 text-blue-700">
                        <span>• {c.temp}°C 이상이면 강도 {c.intensity}%로 작동</span>
                      </div>
                    ))}
                </div>

                <Button onClick={handleAddRule} className="w-full">
                  추가하기
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </CardHeader>

        <CardContent>
          {!autoMode && (
            <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg text-sm text-yellow-700 flex items-center gap-2">
              <Settings className="w-4 h-4 shrink-0" />
              자동 모드가 꺼져 있어 아래 규칙이 적용되지 않습니다.
            </div>
          )}

          <div className="space-y-3">
            {autoRules.map((rule) => {
              const isExpanded = expandedRules.has(rule.id);
              return (
                <div
                  key={rule.id}
                  className={`rounded-lg border-2 transition-all ${
                    rule.enabled && autoMode
                      ? 'bg-blue-50 border-blue-200'
                      : 'bg-gray-50 border-gray-200 opacity-60'
                  }`}
                >
                  {/* Rule header row */}
                  <div className="flex items-center justify-between px-4 py-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                        rule.enabled && autoMode ? 'bg-blue-500' : 'bg-gray-400'
                      }`}>
                        <Wind className="w-4 h-4 text-white" />
                      </div>
                      <div className="min-w-0">
                        <div className="font-medium text-gray-900 flex items-center gap-1.5">
                          <Clock className="w-3.5 h-3.5 text-gray-500 shrink-0" />
                          {rule.timeStart} ~ {rule.timeEnd}
                        </div>
                        <div className="text-xs text-gray-500 mt-0.5">
                          조건 {rule.conditions.length}개 &nbsp;·&nbsp;
                          {[...rule.conditions]
                            .sort((a, b) => a.temp - b.temp)
                            .map((c) => `${c.temp}°C→${c.intensity}%`)
                            .join(', ')}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 shrink-0">
                      <Badge variant={rule.enabled ? 'default' : 'secondary'}>
                        {rule.enabled ? '활성' : '비활성'}
                      </Badge>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => toggleExpand(rule.id)}
                        title={isExpanded ? '접기' : '펼치기'}
                      >
                        {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => handleEditRule(rule)}
                        title="규칙 수정"
                      >
                        <Edit className="w-4 h-4 text-blue-500" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => handleToggleRule(rule.id)}
                        title="활성/비활성 전환"
                      >
                        <Play className={`w-4 h-4 ${rule.enabled ? 'text-blue-500' : 'text-gray-400'}`} />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => handleDeleteRule(rule.id)}
                        title="규칙 삭제"
                      >
                        <Trash2 className="w-4 h-4 text-red-500" />
                      </Button>
                    </div>
                  </div>

                  {/* Expanded conditions table */}
                  {isExpanded && (
                    <div className="px-4 pb-4 pt-0">
                      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                        <div className="grid grid-cols-3 bg-gray-100 px-4 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">
                          <span>기준 온도</span>
                          <span className="text-center">→</span>
                          <span className="text-right">환풍기 강도</span>
                        </div>
                        {[...rule.conditions]
                          .sort((a, b) => a.temp - b.temp)
                          .map((cond, i) => (
                            <div
                              key={i}
                              className={`grid grid-cols-3 px-4 py-2.5 items-center text-sm ${
                                i < rule.conditions.length - 1 ? 'border-b border-gray-100' : ''
                              }`}
                            >
                              <span className="flex items-center gap-1.5 text-red-600 font-medium">
                                <Thermometer className="w-3.5 h-3.5" />
                                {cond.temp}°C 이상
                              </span>
                              <span className="text-center text-gray-400">
                                <ArrowRight className="w-4 h-4 mx-auto" />
                              </span>
                              <span className="text-right">
                                <Badge
                                  variant="outline"
                                  className={`${
                                    rule.enabled && autoMode ? 'border-blue-300 text-blue-700 bg-blue-50' : 'text-gray-500'
                                  }`}
                                >
                                  <Wind className="w-3 h-3 mr-1" />
                                  강도 {cond.intensity}%
                                </Badge>
                              </span>
                            </div>
                          ))}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}

            {autoRules.length === 0 && (
              <div className="text-center py-8 text-gray-400">
                등록된 자동 제어 규칙이 없습니다. 규칙을 추가해주세요.
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Edit Rule Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>자동 제어 규칙 수정</DialogTitle>
          </DialogHeader>
          {editingRule && (
            <div className="space-y-5 pt-4">
              {/* Time Range */}
              <div>
                <Label className="mb-2 block">적용 시간대</Label>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <Label className="text-xs text-gray-500">시작 시간</Label>
                    <Input
                      type="time"
                      value={editingRule.timeStart}
                      onChange={(e) => setEditingRule({ ...editingRule, timeStart: e.target.value })}
                      className="mt-1"
                    />
                  </div>
                  <div>
                    <Label className="text-xs text-gray-500">종료 시간</Label>
                    <Input
                      type="time"
                      value={editingRule.timeEnd}
                      onChange={(e) => setEditingRule({ ...editingRule, timeEnd: e.target.value })}
                      className="mt-1"
                    />
                  </div>
                </div>
              </div>

              {/* Conditions */}
              <ConditionEditor
                conditions={editingRule.conditions}
                onAdd={handleAddEditCondition}
                onRemove={handleRemoveEditCondition}
                onUpdate={handleUpdateEditCondition}
              />

              {/* Preview */}
              <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-800 space-y-1">
                <div className="font-medium flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5" />
                  {editingRule.timeStart} ~ {editingRule.timeEnd} 적용
                </div>
                {[...editingRule.conditions]
                  .sort((a, b) => a.temp - b.temp)
                  .map((c, i) => (
                    <div key={i} className="pl-4 flex items-center gap-1 text-blue-700">
                      <span>• {c.temp}°C 이상이면 강도 {c.intensity}%로 작동</span>
                    </div>
                  ))}
              </div>

              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>취소</Button>
                <Button className="flex-1" onClick={handleSaveEditRule}>저장하기</Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                <Wind className="w-5 h-5 text-purple-600" />
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">{statistics?.dailyCount ?? 0}</div>
                <div className="text-sm text-gray-500">오늘 작동 횟수</div>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Clock className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">{statistics?.dailyOperatingHours ?? 0}시간</div>
                <div className="text-sm text-gray-500">오늘 총 작동 시간</div>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <TrendingUp className="w-5 h-5 text-blue-600" />
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">{statistics?.averageIntensity ?? 0}%</div>
                <div className="text-sm text-gray-500">평균 작동 강도</div>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                <Settings className="w-5 h-5 text-orange-600" />
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">{statistics?.autoModeRatio ?? 0}%</div>
                <div className="text-sm text-gray-500">자동 모드 비율</div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* History */}
      <Card>
        <CardHeader>
          <CardTitle>환풍 이력</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {history.map((item) => {
              const { date, time } = formatDateTime(item.timestamp);
              return (
                <div
                  key={item.id}
                  className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg border border-gray-200"
                >
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                    item.mode === 'auto' ? 'bg-blue-100' : 'bg-purple-100'
                  }`}>
                    <Wind className={`w-5 h-5 ${item.mode === 'auto' ? 'text-blue-600' : 'text-purple-600'}`} />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium text-gray-900">{item.duration}분 작동</span>
                      <Badge variant={item.mode === 'auto' ? 'default' : 'secondary'}>
                        {item.mode === 'auto' ? '자동' : '수동'}
                      </Badge>
                      <Badge variant="outline">강도 {item.intensity}%</Badge>
                    </div>
                    <div className="text-sm text-gray-500 mt-1 flex items-center gap-2">
                      <Calendar className="w-3 h-3" />
                      {date} {time}
                    </div>
                    {item.trigger && (
                      <div className="text-sm text-blue-600 mt-1 flex items-center gap-1">
                        <Thermometer className="w-3 h-3" />
                        {item.trigger}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}