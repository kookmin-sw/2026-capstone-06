import { useState, useMemo } from "react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "../../components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Plus, Pencil, Trash2, Search } from "lucide-react";
import type { Member } from "./mockData";

interface Props {
  members: Member[];
  setMembers: React.Dispatch<React.SetStateAction<Member[]>>;
}

const empty: Member = { memberId: "", memberName: "", memberPhone: "", role: "USER", regDate: "" };

export function MemberManager({ members, setMembers }: Props) {
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Member | null>(null);
  const [form, setForm] = useState<Member>(empty);

  const filtered = useMemo(
    () => members.filter(m =>
      [m.memberId, m.memberName, m.memberPhone].some(v => v.toLowerCase().includes(search.toLowerCase()))
    ),
    [members, search]
  );

  const openAdd = () => { setEditing(null); setForm(empty); setOpen(true); };
  const openEdit = (m: Member) => { setEditing(m); setForm(m); setOpen(true); };

  const save = () => {
    if (!form.memberId || !form.memberName) return;
    if (editing) {
      setMembers(prev => prev.map(m => m.memberId === editing.memberId ? form : m));
    } else {
      if (members.some(m => m.memberId === form.memberId)) {
        alert("이미 존재하는 아이디입니다");
        return;
      }
      setMembers(prev => [...prev, { ...form, regDate: new Date().toISOString().slice(0, 19).replace("T", " ") }]);
    }
    setOpen(false);
  };

  const remove = (id: string) => {
    if (!confirm(`회원 ${id} 을(를) 삭제하시겠습니까?`)) return;
    setMembers(prev => prev.filter(m => m.memberId !== id));
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="아이디/이름/전화번호 검색" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-9" />
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button onClick={openAdd}><Plus className="w-4 h-4 mr-2" />회원 추가</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>{editing ? "회원 수정" : "회원 추가"}</DialogTitle></DialogHeader>
            <div className="space-y-3">
              <div className="space-y-2">
                <Label>아이디</Label>
                <Input value={form.memberId} disabled={!!editing} onChange={(e) => setForm({ ...form, memberId: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>이름</Label>
                <Input value={form.memberName} onChange={(e) => setForm({ ...form, memberName: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>전화번호</Label>
                <Input value={form.memberPhone} onChange={(e) => setForm({ ...form, memberPhone: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>권한</Label>
                <Select value={form.role} onValueChange={(v: "USER" | "ADMIN") => setForm({ ...form, role: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">USER</SelectItem>
                    <SelectItem value="ADMIN">ADMIN</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setOpen(false)}>취소</Button>
              <Button onClick={save}>저장</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <div className="border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>아이디</TableHead>
              <TableHead>이름</TableHead>
              <TableHead>전화번호</TableHead>
              <TableHead>권한</TableHead>
              <TableHead>가입일</TableHead>
              <TableHead className="text-right">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.map((m) => (
              <TableRow key={m.memberId}>
                <TableCell>{m.memberId}</TableCell>
                <TableCell>{m.memberName}</TableCell>
                <TableCell>{m.memberPhone}</TableCell>
                <TableCell>
                  <Badge variant={m.role === "ADMIN" ? "default" : "secondary"}>{m.role}</Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">{m.regDate}</TableCell>
                <TableCell className="text-right">
                  <Button size="sm" variant="ghost" onClick={() => openEdit(m)}><Pencil className="w-4 h-4" /></Button>
                  <Button size="sm" variant="ghost" onClick={() => remove(m.memberId)}><Trash2 className="w-4 h-4 text-red-600" /></Button>
                </TableCell>
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow><TableCell colSpan={6} className="text-center text-muted-foreground py-8">검색 결과가 없습니다</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
