import { useState, useMemo } from "react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Badge } from "../../components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "../../components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../components/ui/table";
import { Plus, Pencil, Trash2, Search } from "lucide-react";
import { registerByAdmin, updateByAdmin, deleteMember, checkId, type MemberDto } from "../../services/memberApi";
import { toast } from "sonner";

interface Props {
  members: MemberDto[];
  setMembers: React.Dispatch<React.SetStateAction<MemberDto[]>>;
  onReload?: () => void;
}

const empty: any = { seq: 0, memberId: "", memberPw: "", memberName: "", memberPhone: "", roleCode: "USER", roleName: "일반회원", regDate: "", enabled: true };

export function MemberManager({ members, setMembers, onReload }: Props) {
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<MemberDto | null>(null);
  const [form, setForm] = useState<any>(empty);

  const filtered = useMemo(
    () => members.filter(m =>
      [m.memberId, m.memberName, m.memberPhone].some(v => v.toLowerCase().includes(search.toLowerCase()))
    ),
    [members, search]
  );

  const openAdd = () => { setEditing(null); setForm(empty); setOpen(true); };
  const openEdit = (m: MemberDto) => { setEditing(m); setForm({ ...m, memberPw: "" }); setOpen(true); };

  const save = async () => {
    if (!form.memberId || !form.memberName) return;
    try {
      if (editing) {
        await updateByAdmin(form);
        toast.success("회원 정보가 수정되었습니다.");
      } else {
        if (!form.memberPw) {
          toast.error("비밀번호를 입력해주세요.");
          return;
        }
        const available = await checkId(form.memberId);
        if (!available) {
          toast.error("이미 존재하는 아이디입니다.");
          return;
        }
        await registerByAdmin(form);
        toast.success("회원이 추가되었습니다.");
      }
      if (onReload) onReload();
      setOpen(false);
    } catch (error) {
      console.error(error);
      toast.error("저장 중 오류가 발생했습니다.");
    }
  };

  const remove = async (seq: number, id: string) => {
    if (!confirm(`회원 ${id} 을(를) 삭제하시겠습니까?`)) return;
    try {
      await deleteMember(seq, id);
      toast.success("삭제되었습니다.");
      if (onReload) onReload();
    } catch (error) {
      console.error(error);
      toast.error("삭제 중 오류가 발생했습니다.");
    }
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
                <Label>비밀번호</Label>
                <Input type="password" value={form.memberPw || ""} placeholder={editing ? "변경할 경우에만 입력" : "비밀번호 입력 (필수)"} onChange={(e) => setForm({ ...form, memberPw: e.target.value })} />
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
                <Select value={form.roleCode} onValueChange={(v: "USER" | "ADMIN") => setForm({ ...form, roleCode: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">일반회원</SelectItem>
                    <SelectItem value="ADMIN">관리자</SelectItem>
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
              <TableRow key={m.seq}>
                <TableCell>{m.memberId}</TableCell>
                <TableCell>{m.memberName}</TableCell>
                <TableCell>{m.memberPhone}</TableCell>
                <TableCell>
                  <Badge variant={m.roleCode === "ADMIN" ? "default" : "secondary"}>{m.roleName}</Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">{m.regDate}</TableCell>
                <TableCell className="text-right">
                  <Button size="sm" variant="ghost" onClick={() => openEdit(m)}><Pencil className="w-4 h-4" /></Button>
                  <Button size="sm" variant="ghost" onClick={() => remove(m.seq, m.memberId)}><Trash2 className="w-4 h-4 text-red-600" /></Button>
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
