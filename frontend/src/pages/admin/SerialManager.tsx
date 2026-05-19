import { useState, useMemo } from "react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "../../components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Plus, Pencil, Trash2, Search, Wand2, Play, RotateCcw } from "lucide-react";
import { addSerial, updateSerial, deleteSerial, toggleSerialUse, generateSerials } from "../../services/serialApi";
import { toast } from "sonner";
import type { Serial } from "./mockData";

interface Props {
  serials: Serial[];
  setSerials: React.Dispatch<React.SetStateAction<Serial[]>>;
  onReload?: () => void;
}

export function SerialManager({ serials, setSerials, onReload }: Props) {
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Serial | null>(null);
  const [form, setForm] = useState({ serialNum: "", isUse: false });
  const [genOpen, setGenOpen] = useState(false);
  const [genCount, setGenCount] = useState(5);

  const filtered = useMemo(
    () => serials.filter(s => s.serialNum.toLowerCase().includes(search.toLowerCase())),
    [serials, search]
  );

  const openAdd = () => { setEditing(null); setForm({ serialNum: "", isUse: false }); setOpen(true); };
  const openEdit = (s: Serial) => { setEditing(s); setForm({ serialNum: s.serialNum, isUse: s.isUse }); setOpen(true); };

  const save = async () => {
    if (!form.serialNum) return;
    try {
      if (editing) {
        await updateSerial(form.serialNum, form.isUse);
        toast.success("수정되었습니다.");
      } else {
        await addSerial(form.serialNum, form.isUse);
        toast.success("등록되었습니다.");
      }
      if (onReload) onReload();
      setOpen(false);
    } catch (e) {
      toast.error("저장 중 오류가 발생했습니다.");
      console.error(e);
    }
  };

  const remove = async (seq: number) => {
    if (!confirm("삭제하시겠습니까?")) return;
    try {
      await deleteSerial(seq);
      toast.success("삭제되었습니다.");
      if (onReload) onReload();
    } catch (e) {
      toast.error("삭제 중 오류가 발생했습니다.");
      console.error(e);
    }
  };

  const toggleUse = async (s: Serial, isUse: boolean) => {
    try {
      await toggleSerialUse(s.serialNum, isUse);
      toast.success(isUse ? "사용 처리되었습니다." : "미사용 처리되었습니다.");
      if (onReload) onReload();
    } catch (e) {
      toast.error("상태 변경 중 오류가 발생했습니다.");
      console.error(e);
    }
  };

  const generate = async () => {
    try {
      await generateSerials(genCount);
      toast.success(`${genCount}개의 시리얼이 생성되었습니다.`);
      if (onReload) onReload();
      setGenOpen(false);
    } catch (e) {
      toast.error("자동 생성 중 오류가 발생했습니다.");
      console.error(e);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <div className="relative flex-1 max-w-sm">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="시리얼 번호 검색" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
        </div>
        <div className="flex gap-2">
          <Dialog open={genOpen} onOpenChange={setGenOpen}>
            <DialogTrigger asChild>
              <Button variant="outline"><Wand2 className="w-4 h-4 mr-2" />자동 생성</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>시리얼 자동 생성</DialogTitle></DialogHeader>
              <div className="space-y-2">
                <Label>생성 수량</Label>
                <Input type="number" min={1} max={100} value={genCount} onChange={(e) => setGenCount(Math.max(1, Number(e.target.value)))} />
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setGenOpen(false)}>취소</Button>
                <Button onClick={generate}>{genCount}개 생성</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button onClick={openAdd}><Plus className="w-4 h-4 mr-2" />시리얼 등록</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>{editing ? "시리얼 수정" : "시리얼 등록"}</DialogTitle></DialogHeader>
              <div className="space-y-3">
                <div className="space-y-2">
                  <Label>시리얼 번호</Label>
                  <Input value={form.serialNum} onChange={(e) => setForm({ ...form, serialNum: e.target.value })} placeholder="20251013-DEV-001" />
                </div>
                <div className="flex items-center gap-2">
                  <input id="isUse" type="checkbox" checked={form.isUse} onChange={(e) => setForm({ ...form, isUse: e.target.checked })} />
                  <Label htmlFor="isUse">사용중</Label>
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setOpen(false)}>취소</Button>
                <Button onClick={save}>저장</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <div className="border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>SEQ</TableHead>
              <TableHead>시리얼 번호</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>등록일</TableHead>
              <TableHead className="text-right">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.map((s) => (
              <TableRow key={s.seq}>
                <TableCell>{s.seq}</TableCell>
                <TableCell><code>{s.serialNum}</code></TableCell>
                <TableCell>
                  <Badge className={s.isUse ? "bg-emerald-100 text-emerald-700 hover:bg-emerald-100" : "bg-slate-100 text-slate-700 hover:bg-slate-100"}>
                    {s.isUse ? "사용중" : "미사용"}
                  </Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">{s.regDate}</TableCell>
                <TableCell className="text-right">
                  {s.isUse ? (
                    <Button size="sm" variant="ghost" title="미사용 처리" onClick={() => toggleUse(s, false)}><RotateCcw className="w-4 h-4" /></Button>
                  ) : (
                    <Button size="sm" variant="ghost" title="사용 처리" onClick={() => toggleUse(s, true)}><Play className="w-4 h-4" /></Button>
                  )}
                  <Button size="sm" variant="ghost" onClick={() => openEdit(s)}><Pencil className="w-4 h-4" /></Button>
                  <Button size="sm" variant="ghost" onClick={() => remove(s.seq)}><Trash2 className="w-4 h-4 text-red-600" /></Button>
                </TableCell>
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground py-8">검색 결과가 없습니다</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
