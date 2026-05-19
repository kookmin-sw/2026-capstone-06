import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Users, Hash, Cpu, ArrowRight, Activity, CheckCircle2, AlertCircle } from "lucide-react";
import type { Member, Serial, Device } from "./mockData";

interface Props {
  members: Member[];
  serials: Serial[];
  devices: Device[];
  onNavigate: (tab: "members" | "serials" | "devices") => void;
}

export function AdminOverview({ members, serials, devices, onNavigate }: Props) {
  const usedSerials = serials.filter(s => s.isUse).length;
  const houseDevices = devices.filter(d => d.deviceType === "HOUSE").length;
  const collarDevices = devices.filter(d => d.deviceType === "COLLAR").length;

  return (
    <>
      <Card className="bg-gradient-to-r from-violet-600 to-indigo-600 text-white border-0">
        <CardContent className="p-6 flex items-center justify-between">
          <div>
            <div className="mb-1">🛡️ 관리자 대시보드</div>
            <div className="opacity-90">시스템 전반의 현황을 확인하고 관리하세요</div>
          </div>
          <Badge variant="secondary" className="bg-white/20 text-white hover:bg-white/20 border-0">
            <CheckCircle2 className="w-3 h-3 mr-1" /> 정상 운영중
          </Badge>
        </CardContent>
      </Card>

      <div className="grid md:grid-cols-3 gap-4">
        <SummaryCard
          icon={<Users className="w-5 h-5" />}
          title="등록 회원"
          value={members.length}
          sub={`관리자 ${members.filter(m => m.role === "ADMIN").length}명 · 일반 ${members.filter(m => m.role === "USER").length}명`}
          tone="violet"
          onClick={() => onNavigate("members")}
        />
        <SummaryCard
          icon={<Hash className="w-5 h-5" />}
          title="시리얼 번호"
          value={`${usedSerials} / ${serials.length}`}
          sub={`사용중 ${usedSerials}개 · 미사용 ${serials.length - usedSerials}개`}
          tone="blue"
          onClick={() => onNavigate("serials")}
        />
        <SummaryCard
          icon={<Cpu className="w-5 h-5" />}
          title="등록 장치"
          value={devices.length}
          sub={`하우스 ${houseDevices}대 · 목걸이 ${collarDevices}대`}
          tone="emerald"
          onClick={() => onNavigate("devices")}
        />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><Activity className="w-4 h-4" /> 최근 등록 장치</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {devices.slice(0, 5).map(d => (
              <div key={d.seq} className="flex items-center gap-3 py-2 border-b last:border-0">
                <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${d.deviceType === "HOUSE" ? "bg-violet-50 text-violet-600" : "bg-blue-50 text-blue-600"}`}>
                  {d.deviceType === "HOUSE" ? "🏠" : "📿"}
                </div>
                <div className="flex-1">
                  <div>{d.deviceId} · {d.objectName ?? "-"}</div>
                  <div className="text-muted-foreground">{d.memberId} · {d.serialNum}</div>
                </div>
                <Badge variant="outline">{d.regDate.slice(0, 10)}</Badge>
              </div>
            ))}
            {devices.length === 0 && <div className="text-muted-foreground text-center py-4">등록된 장치가 없습니다</div>}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><AlertCircle className="w-4 h-4" /> 시리얼 현황</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {serials.slice(0, 5).map(s => (
              <div key={s.seq} className="flex items-center gap-3 py-2 border-b last:border-0">
                <code className="flex-1">{s.serialNum}</code>
                <Badge className={s.isUse ? "bg-emerald-100 text-emerald-700 hover:bg-emerald-100" : "bg-slate-100 text-slate-700 hover:bg-slate-100"}>
                  {s.isUse ? "사용중" : "미사용"}
                </Badge>
              </div>
            ))}
            <Button variant="outline" className="w-full" onClick={() => onNavigate("serials")}>
              시리얼 전체 보기 <ArrowRight className="w-4 h-4 ml-2" />
            </Button>
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function SummaryCard({ icon, title, value, sub, tone, onClick }: { icon: React.ReactNode; title: string; value: string | number; sub: string; tone: string; onClick: () => void }) {
  const map: Record<string, string> = {
    violet: "bg-violet-50 text-violet-600",
    blue: "bg-blue-50 text-blue-600",
    emerald: "bg-emerald-50 text-emerald-600",
  };
  return (
    <Card className="cursor-pointer hover:shadow-md transition-shadow" onClick={onClick}>
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-3">
          <div className="text-muted-foreground">{title}</div>
          <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${map[tone]}`}>{icon}</div>
        </div>
        <div style={{ fontSize: 28 }}>{value}</div>
        <div className="text-muted-foreground mt-1">{sub}</div>
      </CardContent>
    </Card>
  );
}
