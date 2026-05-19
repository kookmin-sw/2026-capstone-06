import { useState, useMemo } from "react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "../../components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Plus, Pencil, Trash2, Search } from "lucide-react";
import { createDevice, updateDevice, deleteDevice, type DeviceDto } from "../../services/deviceApi";
import { type MemberDto } from "../../services/memberApi";
import { toast } from "sonner";
import type { Serial } from "./mockData";

interface Props {
  devices: DeviceDto[];
  setDevices: React.Dispatch<React.SetStateAction<DeviceDto[]>>;
  members: MemberDto[];
  serials: Serial[];
  setSerials: React.Dispatch<React.SetStateAction<Serial[]>>;
  onReload?: () => void;
}

const emptyForm = {
  deviceId: "", memberId: "", serialNum: "", objectCode: "", objectName: "", deviceType: "HOUSE", objectBirth: "", isUse: true,
};

export function DeviceManager({ devices, setDevices, members, serials, setSerials, onReload }: Props) {
  const [search, setSearch] = useState("");
  const [filterType, setFilterType] = useState<"ALL" | "HOUSE" | "COLLAR">("ALL");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<DeviceDto | null>(null);
  const [form, setForm] = useState({ ...emptyForm });

  const filtered = useMemo(() => devices.filter(d => {
    const matchSearch = [d.deviceId, d.memberId, d.serialNum, d.objectCode ?? "", d.objectName ?? ""]
      .some(v => v.toLowerCase().includes(search.toLowerCase()));
    const matchType = filterType === "ALL" || d.deviceType === filterType;
    return matchSearch && matchType;
  }), [devices, search, filterType]);

  const availableSerials = serials.filter(s => !s.isUse || (editing && s.serialNum === editing.serialNum));

  const openAdd = () => { setEditing(null); setForm({ ...emptyForm }); setOpen(true); };
  const openEdit = (d: DeviceDto) => {
    setEditing(d);
    setForm({
      deviceId: d.deviceId, memberId: d.memberId, serialNum: d.serialNum,
      objectCode: d.objectCode ?? "", objectName: d.objectName ?? "",
      deviceType: d.deviceType, objectBirth: d.objectBirth ?? "", isUse: d.isUse,
    });
    setOpen(true);
  };

  const save = async () => {
    if (!form.deviceId || !form.memberId || !form.serialNum) {
      toast.error("필수 입력값을 채워주세요 (장치 ID, 회원, 시리얼)");
      return;
    }
    try {
      if (editing) {
        await updateDevice({
          seq: editing.seq,
          deviceId: form.deviceId,
          memberId: form.memberId,
          serialNum: form.serialNum,
          oldSerialNum: editing.serialNum,
          deviceType: form.deviceType,
          objectName: form.objectName,
          objectBirth: form.objectBirth,
          objectCode: form.objectCode,
        });
        toast.success("장치 정보가 수정되었습니다.");
      } else {
        await createDevice({
          deviceId: form.deviceId,
          memberId: form.memberId,
          serialNum: form.serialNum,
          deviceType: form.deviceType,
          objectName: form.objectName,
          objectBirth: form.objectBirth,
          objectCode: form.objectCode,
        });
        toast.success("장치가 등록되었습니다.");
      }
      if (onReload) onReload();
      setOpen(false);
    } catch (error: any) {
      console.error(error);
      const msg = error?.response?.data?.message ?? "저장 중 오류가 발생했습니다.";
      toast.error(msg);
    }
  };

  const remove = async (d: DeviceDto) => {
    if (!confirm(`${d.deviceId} 을(를) 삭제하시겠습니까?`)) return;
    try {
      await deleteDevice(d.seq);
      toast.success("삭제되었습니다.");
      if (onReload) onReload();
    } catch (error) {
      console.error(error);
      toast.error("삭제 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <div className="flex gap-2 flex-1">
          <div className="relative flex-1 max-w-sm">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="장치ID/회원/시리얼/펫 검색" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
          </div>
          <Select value={filterType} onValueChange={(v: "ALL" | "HOUSE" | "COLLAR") => setFilterType(v)}>
            <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">전체 유형</SelectItem>
              <SelectItem value="HOUSE">하우스</SelectItem>
              <SelectItem value="COLLAR">목걸이</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button onClick={openAdd}><Plus className="w-4 h-4 mr-2" />장치 등록</Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader><DialogTitle>{editing ? "장치 수정" : "장치 등록"}</DialogTitle></DialogHeader>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>장치 ID</Label>
                <Input value={form.deviceId} disabled={!!editing} onChange={(e) => setForm({ ...form, deviceId: e.target.value })} placeholder="DEV001" />
              </div>
              <div className="space-y-2">
                <Label>유형</Label>
                <Select value={form.deviceType} onValueChange={(v: "HOUSE" | "COLLAR") => setForm({ ...form, deviceType: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="HOUSE">하우스</SelectItem>
                    <SelectItem value="COLLAR">목걸이</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>회원</Label>
                <Select value={form.memberId} onValueChange={(v) => setForm({ ...form, memberId: v })}>
                  <SelectTrigger><SelectValue placeholder="회원 선택" /></SelectTrigger>
                  <SelectContent>
                    {members.filter(m => m.roleCode === "USER").map(m => (
                      <SelectItem key={m.memberId} value={m.memberId}>{m.memberId} ({m.memberName})</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>시리얼</Label>
                <Select value={form.serialNum} onValueChange={(v) => setForm({ ...form, serialNum: v })}>
                  <SelectTrigger><SelectValue placeholder="시리얼 선택" /></SelectTrigger>
                  <SelectContent>
                    {availableSerials.map(s => (
                      <SelectItem key={s.seq} value={s.serialNum}>{s.serialNum}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>펫 코드</Label>
                <Input value={form.objectCode} onChange={(e) => setForm({ ...form, objectCode: e.target.value })} placeholder="DOG001 (선택)" />
              </div>
              <div className="space-y-2">
                <Label>펫 이름</Label>
                <Input value={form.objectName} onChange={(e) => setForm({ ...form, objectName: e.target.value })} placeholder="초코 (선택)" />
              </div>
              <div className="space-y-2 col-span-2">
                <Label>펫 생년월일</Label>
                <Input type="date" value={form.objectBirth} onChange={(e) => setForm({ ...form, objectBirth: e.target.value })} />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setOpen(false)}>취소</Button>
              <Button onClick={save}>저장</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="border rounded-lg overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>장치 ID</TableHead>
              <TableHead>유형</TableHead>
              <TableHead>회원</TableHead>
              <TableHead>시리얼</TableHead>
              <TableHead>펫</TableHead>
              <TableHead>생년월일</TableHead>
              <TableHead>등록일</TableHead>
              <TableHead className="text-right">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.map((d) => (
              <TableRow key={d.seq}>
                <TableCell><code>{d.deviceId}</code></TableCell>
                <TableCell>
                  <Badge variant={d.deviceType === "HOUSE" ? "default" : "secondary"}>
                    {d.deviceType === "HOUSE" ? "하우스" : "목걸이"}
                  </Badge>
                </TableCell>
                <TableCell>{d.memberId}</TableCell>
                <TableCell><code>{d.serialNum}</code></TableCell>
                <TableCell>{d.objectName ?? "-"}{d.objectCode ? ` (${d.objectCode})` : ""}</TableCell>
                <TableCell className="text-muted-foreground">{d.objectBirth ?? "-"}</TableCell>
                <TableCell className="text-muted-foreground">{d.regDate}</TableCell>
                <TableCell className="text-right">
                  <Button size="sm" variant="ghost" onClick={() => openEdit(d)}><Pencil className="w-4 h-4" /></Button>
                  <Button size="sm" variant="ghost" onClick={() => remove(d)}><Trash2 className="w-4 h-4 text-red-600" /></Button>
                </TableCell>
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow><TableCell colSpan={8} className="text-center text-muted-foreground py-8">검색 결과가 없습니다</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}


interface Props {
  devices: Device[];
  setDevices: React.Dispatch<React.SetStateAction<Device[]>>;
  members: Member[];
  serials: Serial[];
  setSerials: React.Dispatch<React.SetStateAction<Serial[]>>;
}

const emptyForm: Omit<Device, "seq" | "regDate"> = {
  deviceId: "", memberId: "", serialNum: "", objectCode: "", objectName: "", deviceType: "HOUSE", objectBirth: "", isUse: true,
};

export function DeviceManager({ devices, setDevices, members, serials, setSerials }: Props) {
  const [search, setSearch] = useState("");
  const [filterType, setFilterType] = useState<"ALL" | "HOUSE" | "COLLAR">("ALL");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Device | null>(null);
  const [form, setForm] = useState({ ...emptyForm });

  const filtered = useMemo(() => devices.filter(d => {
    const matchSearch = [d.deviceId, d.memberId, d.serialNum, d.objectCode, d.objectName ?? ""]
      .some(v => v.toLowerCase().includes(search.toLowerCase()));
    const matchType = filterType === "ALL" || d.deviceType === filterType;
    return matchSearch && matchType;
  }), [devices, search, filterType]);

  const availableSerials = serials.filter(s => !s.isUse || (editing && s.serialNum === editing.serialNum));

  const openAdd = () => { setEditing(null); setForm({ ...emptyForm }); setOpen(true); };
  const openEdit = (d: Device) => {
    setEditing(d);
    setForm({
      deviceId: d.deviceId, memberId: d.memberId, serialNum: d.serialNum, objectCode: d.objectCode,
      objectName: d.objectName ?? "", deviceType: d.deviceType, objectBirth: d.objectBirth, isUse: d.isUse,
    });
    setOpen(true);
  };

  const save = () => {
    if (!form.deviceId || !form.memberId || !form.serialNum) {
      alert("필수 입력값을 채워주세요");
      return;
    }
    if (editing) {
      const oldSerial = editing.serialNum;
      setDevices(prev => prev.map(d => d.seq === editing.seq ? { ...editing, ...form } : d));
      if (oldSerial !== form.serialNum) {
        setSerials(prev => prev.map(s => s.serialNum === oldSerial ? { ...s, isUse: false } : s.serialNum === form.serialNum ? { ...s, isUse: true } : s));
      }
    } else {
      const seq = Math.max(0, ...devices.map(d => d.seq)) + 1;
      setDevices(prev => [...prev, { ...form, seq, regDate: new Date().toISOString().slice(0, 19).replace("T", " ") }]);
      setSerials(prev => prev.map(s => s.serialNum === form.serialNum ? { ...s, isUse: true } : s));
    }
    setOpen(false);
  };

  const remove = (d: Device) => {
    if (!confirm(`${d.deviceId} 을(를) 삭제하시겠습니까?`)) return;
    setDevices(prev => prev.filter(x => x.seq !== d.seq));
    setSerials(prev => prev.map(s => s.serialNum === d.serialNum ? { ...s, isUse: false } : s));
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <div className="flex gap-2 flex-1">
          <div className="relative flex-1 max-w-sm">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="장치ID/회원/시리얼/펫 검색" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
          </div>
          <Select value={filterType} onValueChange={(v: "ALL" | "HOUSE" | "COLLAR") => setFilterType(v)}>
            <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">전체 유형</SelectItem>
              <SelectItem value="HOUSE">하우스</SelectItem>
              <SelectItem value="COLLAR">목걸이</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button onClick={openAdd}><Plus className="w-4 h-4 mr-2" />장치 등록</Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader><DialogTitle>{editing ? "장치 수정" : "장치 등록"}</DialogTitle></DialogHeader>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>장치 ID</Label>
                <Input value={form.deviceId} onChange={(e) => setForm({ ...form, deviceId: e.target.value })} placeholder="DEV001" />
              </div>
              <div className="space-y-2">
                <Label>유형</Label>
                <Select value={form.deviceType} onValueChange={(v: "HOUSE" | "COLLAR") => setForm({ ...form, deviceType: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="HOUSE">하우스</SelectItem>
                    <SelectItem value="COLLAR">목걸이</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>회원</Label>
                <Select value={form.memberId} onValueChange={(v) => setForm({ ...form, memberId: v })}>
                  <SelectTrigger><SelectValue placeholder="회원 선택" /></SelectTrigger>
                  <SelectContent>
                    {members.filter(m => m.role === "USER").map(m => (
                      <SelectItem key={m.memberId} value={m.memberId}>{m.memberId} ({m.memberName})</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>시리얼</Label>
                <Select value={form.serialNum} onValueChange={(v) => setForm({ ...form, serialNum: v })}>
                  <SelectTrigger><SelectValue placeholder="시리얼 선택" /></SelectTrigger>
                  <SelectContent>
                    {availableSerials.map(s => (
                      <SelectItem key={s.seq} value={s.serialNum}>{s.serialNum}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>펫 코드</Label>
                <Input value={form.objectCode} onChange={(e) => setForm({ ...form, objectCode: e.target.value })} placeholder="DOG001" />
              </div>
              <div className="space-y-2">
                <Label>펫 이름</Label>
                <Input value={form.objectName ?? ""} onChange={(e) => setForm({ ...form, objectName: e.target.value })} placeholder="초코" />
              </div>
              <div className="space-y-2 col-span-2">
                <Label>펫 생년월일</Label>
                <Input type="date" value={form.objectBirth} onChange={(e) => setForm({ ...form, objectBirth: e.target.value })} />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setOpen(false)}>취소</Button>
              <Button onClick={save}>저장</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="border rounded-lg overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>장치 ID</TableHead>
              <TableHead>유형</TableHead>
              <TableHead>회원</TableHead>
              <TableHead>시리얼</TableHead>
              <TableHead>펫</TableHead>
              <TableHead>생년월일</TableHead>
              <TableHead>등록일</TableHead>
              <TableHead className="text-right">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.map((d) => (
              <TableRow key={d.seq}>
                <TableCell><code>{d.deviceId}</code></TableCell>
                <TableCell>
                  <Badge variant={d.deviceType === "HOUSE" ? "default" : "secondary"}>
                    {d.deviceType === "HOUSE" ? "하우스" : "목걸이"}
                  </Badge>
                </TableCell>
                <TableCell>{d.memberId}</TableCell>
                <TableCell><code>{d.serialNum}</code></TableCell>
                <TableCell>{d.objectName ?? "-"} ({d.objectCode})</TableCell>
                <TableCell className="text-muted-foreground">{d.objectBirth}</TableCell>
                <TableCell className="text-muted-foreground">{d.regDate}</TableCell>
                <TableCell className="text-right">
                  <Button size="sm" variant="ghost" onClick={() => openEdit(d)}><Pencil className="w-4 h-4" /></Button>
                  <Button size="sm" variant="ghost" onClick={() => remove(d)}><Trash2 className="w-4 h-4 text-red-600" /></Button>
                </TableCell>
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow><TableCell colSpan={8} className="text-center text-muted-foreground py-8">검색 결과가 없습니다</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
