import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "../../components/ui/dialog";
import {
    Settings,
    Cpu,
    Hash,
    User,
    Barcode,
    Tag,
    CalendarDays,
    ClipboardList,
    Search,
    CheckCircle2,
    XCircle,
    ChevronRight,
    RefreshCw,
    Info,
    Phone,
    Mail,
    ShieldAlert,
    ShieldCheck,
    Edit3,
    Lock,
    LogOut,
    AlertTriangle,
    Clock,
    RotateCcw,
    Save,
    Eye,
    EyeOff,
    UserX,
    UserCheck,
} from "lucide-react";
import { toast } from "sonner";
import { useAuthStore } from "../../store/authStore";
import * as memberApi from "../../services/memberApi";
import * as dashboardApi from "../../services/dashboardApi";

// ─────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────

interface DeviceInfo {
    seq: number;
    deviceId: string;
    memberId: string;
    serialNum: string;
    deviceType: string;
    regDate: string;
    isUse: boolean;
}

interface MemberInfo {
    memberId: string;
    memberName: string;
    memberPhone: string;
    memberEmail: string;
    memberSeq: number;
    regDate: string;
    role: string;
}

interface WithdrawStatus {
    withdrawId: number;
    memberId: string;
    memberSeq: number;
    withdrawDate: string;
    expireDate: string;
    isWithdrawn: number;
    withdrawn: boolean;
}


// ─────────────────────────────────────────────
// Device field metadata
// ─────────────────────────────────────────────

const FIELD_META: {
    key: keyof DeviceInfo;
    label: string;
    icon: React.ElementType;
    description: string;
}[] = [
        { key: "seq", label: "시퀀스 번호", icon: Hash, description: "디바이스 고유 순번" },
        { key: "deviceId", label: "디바이스 ID", icon: Cpu, description: "시스템 내 디바이스 식별자" },
        { key: "memberId", label: "멤버 ID", icon: User, description: "디바이스 소유 회원 ID" },
        { key: "serialNum", label: "시리얼 번호", icon: Barcode, description: "하드웨어 고유 시리얼 번호" },
        { key: "deviceType", label: "디바이스 타입", icon: Tag, description: "펫하우스 모델 및 버전" },
        { key: "regDate", label: "등록일", icon: ClipboardList, description: "디바이스 최초 등록 일시" },
        { key: "isUse", label: "사용 여부", icon: CheckCircle2, description: "현재 디바이스 활성화 상태" },
    ];

function formatDeviceValue(key: keyof DeviceInfo, value: DeviceInfo[keyof DeviceInfo]): React.ReactNode {
    if (key === "isUse") {
        return value ? (
            <Badge className="bg-green-100 text-green-700 border-green-200 hover:bg-green-100">
                <CheckCircle2 className="w-3 h-3 mr-1" /> 사용 중
            </Badge>
        ) : (
            <Badge variant="secondary" className="bg-gray-100 text-gray-500 border-gray-200">
                <XCircle className="w-3 h-3 mr-1" /> 미사용
            </Badge>
        );
    }
    if (key === "regDate") {
        const d = new Date(value as string);
        return <span className="text-gray-800">{d.toLocaleDateString("ko-KR")} {d.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}</span>;
    }
    if (key === "seq") {
        return <span className="font-mono text-gray-800">#{String(value).padStart(4, "0")}</span>;
    }
    return <span className="font-mono text-gray-800 break-all">{String(value)}</span>;
}

// ─────────────────────────────────────────────
// Member Tab
// ─────────────────────────────────────────────

function MemberTab() {
    const { memberId } = useAuthStore();
    const [member, setMember] = useState<MemberInfo | null>(null);
    const [withdrawStatus, setWithdrawStatus] = useState<WithdrawStatus | null>(null);
    const [loading, setLoading] = useState(true);

    // Edit state
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [editForm, setEditForm] = useState({ memberName: "", memberPhone: "" });
    const [editLoading, setEditLoading] = useState(false);

    // Withdraw state
    const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
    const [withdrawPassword, setWithdrawPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [withdrawLoading, setWithdrawLoading] = useState(false);

    // Cancel withdraw state
    const [isCancelOpen, setIsCancelOpen] = useState(false);
    const [cancelLoading, setCancelLoading] = useState(false);

    useEffect(() => {
        const fetchMemberData = async () => {
            if (!memberId) {
                setLoading(false);
                return;
            }
            try {
                const res = await memberApi.getMemberByMemberId(memberId);
                const memberInfo: MemberInfo = {
                    memberId: res.memberId,
                    memberName: res.memberName,
                    memberPhone: res.memberPhone,
                    memberEmail: `${res.memberId}@example.com`,
                    memberSeq: res.seq,
                    regDate: res.regDate,
                    role: res.roleName,
                };
                setMember(memberInfo);
                setEditForm({ memberName: res.memberName, memberPhone: res.memberPhone });

                // Check activation status
                const statusRes = await memberApi.getAccountStatus(memberId);
                if (!statusRes.enabled) {
                    setWithdrawStatus({
                        withdrawId: res.seq,
                        memberId: res.memberId,
                        memberSeq: res.seq,
                        withdrawDate: res.regDate,
                        expireDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
                        isWithdrawn: 1,
                        withdrawn: true,
                    });
                } else {
                    setWithdrawStatus(null);
                }
            } catch (error) {
                console.error("Failed to fetch member info:", error);
                toast.error("회원 정보를 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        fetchMemberData();
    }, [memberId]);

    // ── Edit Member Info ──────────────────────────────────────
    const handleEditSave = async () => {
        if (!member) return;
        if (!editForm.memberName.trim()) { toast.error("이름을 입력해주세요"); return; }
        if (!editForm.memberPhone.trim()) { toast.error("전화번호를 입력해주세요"); return; }
        setEditLoading(true);
        try {
            await memberApi.updateMember({
                seq: member.memberSeq,
                member_id: member.memberId,
                member_name: editForm.memberName,
                member_phone: editForm.memberPhone,
            });
            setMember((prev) => prev ? { ...prev, memberName: editForm.memberName, memberPhone: editForm.memberPhone } : null);
            setIsEditOpen(false);
            toast.success("회원 정보가 수정되었습니다");
        } catch (error) {
            console.error("Failed to update member info:", error);
            toast.error("회원 정보 수정에 실패했습니다.");
        } finally {
            setEditLoading(false);
        }
    };

    // ── Withdraw Apply ───────────────────────────────────────
    const handleWithdraw = async () => {
        if (!member) return;
        if (!withdrawPassword.trim()) { toast.error("비밀번호를 입력해주세요"); return; }
        setWithdrawLoading(true);
        try {
            await memberApi.deactivateMember(member.memberId, withdrawPassword);
            const expireDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
            const now = new Date();
            const mockWithdrawStatus: WithdrawStatus = {
                withdrawId: member.memberSeq,
                memberId: member.memberId,
                memberSeq: member.memberSeq,
                withdrawDate: now.toISOString(),
                expireDate: expireDate.toISOString(),
                isWithdrawn: 1,
                withdrawn: true,
            };
            setWithdrawStatus(mockWithdrawStatus);
            setIsWithdrawOpen(false);
            setWithdrawPassword("");
            toast.success("탈퇴 신청이 완료되었습니다. 계정이 비활성화되었습니다.");
        } catch (error: any) {
            console.error("Deactivation failed:", error);
            const errMsg = error?.response?.data?.message || "비밀번호 확인 또는 탈퇴 처리에 실패했습니다.";
            toast.error(errMsg);
        } finally {
            setWithdrawLoading(false);
        }
    };

    // ── Cancel Withdraw ──────────────────────────────────────
    const handleCancelWithdraw = async () => {
        if (!member) return;
        setCancelLoading(true);
        try {
            await memberApi.reactivateMember(member.memberId);
            setWithdrawStatus(null);
            setIsCancelOpen(false);
            toast.success("탈퇴 신청이 취소되었습니다. 계정이 정상 복원되었습니다.");
        } catch (error) {
            console.error("Reactivation failed:", error);
            toast.error("탈퇴 신청 취소에 실패했습니다.");
        } finally {
            setCancelLoading(false);
        }
    };

    const formatDate = (iso: string) => {
        const d = new Date(iso);
        return `${d.toLocaleDateString("ko-KR")} ${d.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}`;
    };

    const daysLeft = withdrawStatus
        ? Math.max(0, Math.ceil((new Date(withdrawStatus.expireDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24)))
        : 0;

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center py-20 gap-3">
                <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
                <p className="text-sm text-gray-500 font-medium">회원 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (!member) {
        return (
            <div className="text-center py-20 text-gray-500 font-medium">
                로그인 세션이 만료되었거나 회원 정보를 찾을 수 없습니다.
            </div>
        );
    }

    return (
        <div className="space-y-6">

            {/* ── Withdrawal Warning Banner ────────────────────────── */}
            {withdrawStatus && (
                <div className="flex items-start gap-4 p-4 bg-red-50 border-2 border-red-200 rounded-xl">
                    <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
                        <ShieldAlert className="w-5 h-5 text-red-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                        <div className="font-semibold text-red-800 mb-1">탈퇴 신청 진행 중</div>
                        <div className="text-sm text-red-700 space-y-0.5">
                            <div className="flex items-center gap-2">
                                <Clock className="w-3.5 h-3.5 flex-shrink-0" />
                                <span>신청일: {formatDate(withdrawStatus.withdrawDate)}</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <AlertTriangle className="w-3.5 h-3.5 flex-shrink-0" />
                                <span>탈퇴 완료 예정: {formatDate(withdrawStatus.expireDate)} (남은 기간: <strong>{daysLeft}일</strong>)</span>
                            </div>
                        </div>
                    </div>
                    <Button
                        size="sm"
                        variant="outline"
                        className="border-red-300 text-red-700 hover:bg-red-100 flex-shrink-0"
                        onClick={() => setIsCancelOpen(true)}
                    >
                        <RotateCcw className="w-3.5 h-3.5 mr-1.5" />
                        신청 취소
                    </Button>
                </div>
            )}

            {/* ── Member Info Card ─────────────────────────────────── */}
            <Card>
                <CardHeader className="pb-4">
                    <div className="flex items-center justify-between">
                        <CardTitle className="flex items-center gap-2">
                            <User className="w-5 h-5 text-blue-600" />
                            회원 정보
                        </CardTitle>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => {
                                setEditForm({ memberName: member.memberName, memberPhone: member.memberPhone });
                                setIsEditOpen(true);
                            }}
                            className="gap-1.5"
                        >
                            <Edit3 className="w-3.5 h-3.5" />
                            정보 수정
                        </Button>
                    </div>
                </CardHeader>
                <CardContent className="pt-0">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {/* Member ID */}
                        <InfoRow
                            icon={<Hash className="w-4 h-4 text-blue-500" />}
                            label="아이디"
                            value={member.memberId}
                            mono
                        />
                        {/* Name */}
                        <InfoRow
                            icon={<User className="w-4 h-4 text-purple-500" />}
                            label="이름"
                            value={member.memberName}
                        />
                        {/* Phone */}
                        <InfoRow
                            icon={<Phone className="w-4 h-4 text-green-500" />}
                            label="전화번호"
                            value={member.memberPhone}
                        />
                        {/* Email */}
                        <InfoRow
                            icon={<Mail className="w-4 h-4 text-orange-500" />}
                            label="이메일"
                            value={member.memberEmail}
                        />
                        {/* Seq */}
                        <InfoRow
                            icon={<ClipboardList className="w-4 h-4 text-gray-400" />}
                            label="회원 번호"
                            value={`#${String(member.memberSeq).padStart(6, "0")}`}
                            mono
                        />
                        {/* Reg date */}
                        <InfoRow
                            icon={<CalendarDays className="w-4 h-4 text-gray-400" />}
                            label="가입일"
                            value={formatDate(member.regDate)}
                        />
                        {/* Role */}
                        <InfoRow
                            icon={<ShieldCheck className="w-4 h-4 text-blue-400" />}
                            label="권한"
                            value={
                                <Badge variant="outline" className="font-mono text-xs">
                                    {member.role}
                                </Badge>
                            }
                        />
                        {/* Account status */}
                        <InfoRow
                            icon={<UserCheck className="w-4 h-4 text-green-500" />}
                            label="계정 상태"
                            value={
                                withdrawStatus ? (
                                    <Badge className="bg-red-100 text-red-700 border-red-200 hover:bg-red-100 text-xs">
                                        <UserX className="w-3 h-3 mr-1" />탈퇴 신청 중
                                    </Badge>
                                ) : (
                                    <Badge className="bg-green-100 text-green-700 border-green-200 hover:bg-green-100 text-xs">
                                        <CheckCircle2 className="w-3 h-3 mr-1" />정상
                                    </Badge>
                                )
                            }
                        />
                    </div>
                </CardContent>
            </Card>

            {/* ── Withdrawal Status Card ───────────────────────────── */}
            <Card className={withdrawStatus ? "border-red-200" : "border-gray-200"}>
                <CardHeader className="pb-4">
                    <CardTitle className={`flex items-center gap-2 ${withdrawStatus ? "text-red-700" : "text-gray-700"}`}>
                        {withdrawStatus
                            ? <ShieldAlert className="w-5 h-5 text-red-500" />
                            : <ShieldCheck className="w-5 h-5 text-gray-400" />}
                        탈퇴 상태 조회
                    </CardTitle>
                </CardHeader>
                <CardContent className="pt-0">
                    {withdrawStatus ? (
                        <div className="space-y-3">
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 p-4 bg-red-50 rounded-xl border border-red-100">
                                <InfoRow icon={<Hash className="w-4 h-4 text-red-400" />} label="탈퇴 신청 ID" value={`#${withdrawStatus.withdrawId}`} mono />
                                <InfoRow icon={<User className="w-4 h-4 text-red-400" />} label="회원 ID" value={withdrawStatus.memberId} mono />
                                <InfoRow icon={<CalendarDays className="w-4 h-4 text-red-400" />} label="신청일시" value={formatDate(withdrawStatus.withdrawDate)} />
                                <InfoRow
                                    icon={<Clock className="w-4 h-4 text-red-500" />}
                                    label="탈퇴 완료 예정일"
                                    value={
                                        <span className="text-red-700 font-semibold">{formatDate(withdrawStatus.expireDate)}</span>
                                    }
                                />
                            </div>
                            {/* Progress bar */}
                            <div className="space-y-1.5">
                                <div className="flex justify-between text-xs text-gray-500">
                                    <span>유예 기간</span>
                                    <span className="font-medium text-red-600">남은 기간 {daysLeft}일</span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2">
                                    <div
                                        className="bg-red-500 h-2 rounded-full transition-all"
                                        style={{ width: `${Math.max(0, Math.min(100, ((7 - daysLeft) / 7) * 100))}%` }}
                                    />
                                </div>
                                <div className="flex justify-between text-xs text-gray-400">
                                    <span>{formatDate(withdrawStatus.withdrawDate).split(" ")[0]}</span>
                                    <span>{formatDate(withdrawStatus.expireDate).split(" ")[0]}</span>
                                </div>
                            </div>
                            <Button
                                variant="outline"
                                className="w-full border-red-200 text-red-700 hover:bg-red-50 gap-2"
                                onClick={() => setIsCancelOpen(true)}
                            >
                                <RotateCcw className="w-4 h-4" />
                                탈퇴 신청 취소하기
                            </Button>
                        </div>
                    ) : (
                        <div className="flex flex-col items-center py-6 text-center gap-3">
                            <div className="w-14 h-14 bg-green-50 rounded-full flex items-center justify-center">
                                <ShieldCheck className="w-7 h-7 text-green-500" />
                            </div>
                            <div>
                                <div className="font-medium text-gray-700">탈퇴 신청 내역 없음</div>
                                <div className="text-sm text-gray-400 mt-1">현재 정상적으로 서비스를 이용 중입니다</div>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* ── Danger Zone ─────────────────────────────────────── */}
            <Card className="border-red-200">
                <CardHeader className="pb-3">
                    <CardTitle className="flex items-center gap-2 text-red-700">
                        <AlertTriangle className="w-5 h-5" />
                        위험 구역
                    </CardTitle>
                </CardHeader>
                <CardContent className="pt-0">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-4 bg-red-50 rounded-xl border border-red-100">
                        <div>
                            <div className="font-medium text-red-800">회원 탈퇴 신청</div>
                            <div className="text-sm text-red-600 mt-1">
                                탈퇴 신청 후 7일간 유예기간이 적용됩니다. 유예기간 내 취소 가능합니다.
                            </div>
                        </div>
                        <Button
                            variant="outline"
                            className="border-red-300 text-red-700 hover:bg-red-100 gap-2 flex-shrink-0 whitespace-nowrap"
                            onClick={() => setIsWithdrawOpen(true)}
                            disabled={!!withdrawStatus}
                        >
                            <LogOut className="w-4 h-4" />
                            {withdrawStatus ? "탈퇴 신청 중" : "탈퇴 신청"}
                        </Button>
                    </div>
                </CardContent>
            </Card>

            {/* ── Edit Member Dialog ───────────────────────────────── */}
            <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                <DialogContent className="max-w-md">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            <Edit3 className="w-5 h-5 text-blue-600" />
                            회원 정보 수정
                        </DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 pt-2">
                        <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                            <div className="text-xs text-gray-500 mb-1">아이디 (변경 불가)</div>
                            <div className="font-mono text-sm text-gray-700">{member.memberId}</div>
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="edit-name">이름</Label>
                            <Input
                                id="edit-name"
                                value={editForm.memberName}
                                onChange={(e) => setEditForm({ ...editForm, memberName: e.target.value })}
                                placeholder="홍길동"
                            />
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="edit-phone">전화번호</Label>
                            <Input
                                id="edit-phone"
                                value={editForm.memberPhone}
                                onChange={(e) => setEditForm({ ...editForm, memberPhone: e.target.value })}
                                placeholder="010-0000-0000"
                            />
                        </div>
                        <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-700">
                            💡 수정된 정보는 즉시 반영됩니다.
                        </div>
                        <div className="flex gap-2 pt-1">
                            <Button variant="outline" className="flex-1" onClick={() => setIsEditOpen(false)}>취소</Button>
                            <Button className="flex-1 gap-2" onClick={handleEditSave} disabled={editLoading}>
                                {editLoading ? (
                                    <RefreshCw className="w-4 h-4 animate-spin" />
                                ) : (
                                    <Save className="w-4 h-4" />
                                )}
                                {editLoading ? "저장 중..." : "저장하기"}
                            </Button>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>

            {/* ── Withdraw Apply Dialog ────────────────────────────── */}
            <Dialog open={isWithdrawOpen} onOpenChange={(o) => { setIsWithdrawOpen(o); if (!o) setWithdrawPassword(""); }}>
                <DialogContent className="max-w-md">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2 text-red-700">
                            <UserX className="w-5 h-5" />
                            회원 탈퇴 신청
                        </DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 pt-2">
                        <div className="p-4 bg-red-50 border border-red-200 rounded-xl space-y-2 text-sm text-red-700">
                            <div className="font-semibold flex items-center gap-1.5">
                                <AlertTriangle className="w-4 h-4" /> 탈퇴 전 반드시 확인하세요
                            </div>
                            <ul className="space-y-1 pl-5 list-disc text-red-600">
                                <li>탈퇴 신청 후 <strong>7일 유예기간</strong>이 적용됩니다.</li>
                                <li>유예기간 내 언제든지 탈퇴를 취소할 수 있습니다.</li>
                                <li>유예기간 만료 후 모든 데이터가 삭제되며 복구할 수 없습니다.</li>
                                <li>등록된 펫하우스 기기 및 데이터도 함께 삭제됩니다.</li>
                            </ul>
                        </div>

                        <div className="space-y-1.5">
                            <Label htmlFor="withdraw-pw" className="flex items-center gap-1.5">
                                <Lock className="w-3.5 h-3.5" />
                                비밀번호 확인
                            </Label>
                            <div className="relative">
                                <Input
                                    id="withdraw-pw"
                                    type={showPassword ? "text" : "password"}
                                    value={withdrawPassword}
                                    onChange={(e) => setWithdrawPassword(e.target.value)}
                                    placeholder="현재 비밀번호를 입력하세요"
                                    className="pr-10"
                                />
                                <button
                                    type="button"
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                    onClick={() => setShowPassword(!showPassword)}
                                >
                                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        <div className="flex gap-2 pt-1">
                            <Button variant="outline" className="flex-1" onClick={() => { setIsWithdrawOpen(false); setWithdrawPassword(""); }}>
                                취소
                            </Button>
                            <Button
                                className="flex-1 gap-2 bg-red-600 hover:bg-red-700"
                                onClick={handleWithdraw}
                                disabled={withdrawLoading}
                            >
                                {withdrawLoading ? (
                                    <RefreshCw className="w-4 h-4 animate-spin" />
                                ) : (
                                    <LogOut className="w-4 h-4" />
                                )}
                                {withdrawLoading ? "처리 중..." : "탈퇴 신청"}
                            </Button>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>

            {/* ── Cancel Withdraw Dialog ───────────────────────────── */}
            <Dialog open={isCancelOpen} onOpenChange={setIsCancelOpen}>
                <DialogContent className="max-w-md">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2 text-blue-700">
                            <RotateCcw className="w-5 h-5" />
                            탈퇴 신청 취소
                        </DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 pt-2">
                        <div className="p-4 bg-blue-50 border border-blue-200 rounded-xl text-sm text-blue-700">
                            <div className="font-semibold mb-1">탈퇴 신청을 취소하시겠습니까?</div>
                            <div>취소하면 계정이 정상 상태로 복원되며, 모든 서비스를 계속 이용하실 수 있습니다.</div>
                        </div>
                        {withdrawStatus && (
                            <div className="grid grid-cols-2 gap-3 text-sm">
                                <div className="p-3 bg-gray-50 rounded-lg border border-gray-200">
                                    <div className="text-xs text-gray-400 mb-1">신청일</div>
                                    <div className="text-gray-700 font-medium">{formatDate(withdrawStatus.withdrawDate)}</div>
                                </div>
                                <div className="p-3 bg-gray-50 rounded-lg border border-gray-200">
                                    <div className="text-xs text-gray-400 mb-1">남은 유예기간</div>
                                    <div className="text-red-600 font-semibold">{daysLeft}일</div>
                                </div>
                            </div>
                        )}
                        <div className="flex gap-2 pt-1">
                            <Button variant="outline" className="flex-1" onClick={() => setIsCancelOpen(false)}>닫기</Button>
                            <Button className="flex-1 gap-2" onClick={handleCancelWithdraw} disabled={cancelLoading}>
                                {cancelLoading ? (
                                    <RefreshCw className="w-4 h-4 animate-spin" />
                                ) : (
                                    <RotateCcw className="w-4 h-4" />
                                )}
                                {cancelLoading ? "처리 중..." : "탈퇴 신청 취소"}
                            </Button>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    );
}

// ─────────────────────────────────────────────
// InfoRow helper
// ─────────────────────────────────────────────

function InfoRow({
    icon,
    label,
    value,
    mono = false,
}: {
    icon: React.ReactNode;
    label: string;
    value: React.ReactNode;
    mono?: boolean;
}) {
    return (
        <div className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg border border-gray-100">
            <div className="w-7 h-7 bg-white rounded-md border border-gray-200 flex items-center justify-center flex-shrink-0 mt-0.5">
                {icon}
            </div>
            <div className="min-w-0">
                <div className="text-xs text-gray-400 mb-0.5">{label}</div>
                <div className={`text-sm text-gray-800 ${mono ? "font-mono" : "font-medium"} break-all`}>
                    {value}
                </div>
            </div>
        </div>
    );
}

// ─────────────────────────────────────────────
// Device Tab
// ─────────────────────────────────────────────

function DeviceTab() {
    const { memberId } = useAuthStore();
    const [devices, setDevices] = useState<DeviceInfo[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");
    const [selectedDevice, setSelectedDevice] = useState<DeviceInfo | null>(null);
    const [filterUse, setFilterUse] = useState<"all" | "active" | "inactive">("all");

    const fetchDevices = async (showToast = false) => {
        if (!memberId) {
            setLoading(false);
            return;
        }
        try {
            const res = await dashboardApi.getDevices(memberId);
            const mapped: DeviceInfo[] = res.map((d: any) => ({
                seq: d.seq,
                deviceId: d.deviceId,
                memberId: d.memberId,
                serialNum: d.serialNum,
                deviceType: d.deviceType,
                regDate: d.regDate,
                isUse: d.isUse,
            }));
            setDevices(mapped);
            if (showToast) {
                toast.success("디바이스 목록이 갱신되었습니다.");
            }
        } catch (error) {
            console.error("Failed to fetch devices:", error);
            toast.error("디바이스 목록을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDevices();
    }, [memberId]);

    const filtered = devices.filter((d) => {
        const matchSearch =
            search === "" ||
            Object.values(d).some((v) => String(v).toLowerCase().includes(search.toLowerCase()));
        const matchFilter =
            filterUse === "all" ||
            (filterUse === "active" && d.isUse) ||
            (filterUse === "inactive" && !d.isUse);
        return matchSearch && matchFilter;
    });

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center py-20 gap-3">
                <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
                <p className="text-sm text-gray-500 font-medium">디바이스 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Summary cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <Card>
                    <CardContent className="pt-5">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                <Cpu className="w-5 h-5 text-blue-600" />
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{devices.length}</div>
                                <div className="text-xs text-gray-500">전체 디바이스</div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="pt-5">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                <CheckCircle2 className="w-5 h-5 text-green-600" />
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{devices.filter((d) => d.isUse).length}</div>
                                <div className="text-xs text-gray-500">사용 중</div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="pt-5">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                <XCircle className="w-5 h-5 text-gray-400" />
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{devices.filter((d) => !d.isUse).length}</div>
                                <div className="text-xs text-gray-500">미사용</div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardContent className="pt-5">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                <Tag className="w-5 h-5 text-purple-600" />
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-gray-900">{new Set(devices.map((d) => d.deviceType)).size}</div>
                                <div className="text-xs text-gray-500">디바이스 타입</div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Device list */}
            <Card>
                <CardHeader className="pb-4">
                    <div className="flex flex-col sm:flex-row sm:items-center gap-3">
                        <CardTitle className="flex items-center gap-2 flex-1">
                            <Settings className="w-5 h-5 text-blue-600" />
                            디바이스 정보
                        </CardTitle>
                        <div className="flex gap-1">
                            {(["all", "active", "inactive"] as const).map((f) => (
                                <Button
                                    key={f}
                                    size="sm"
                                    variant={filterUse === f ? "default" : "outline"}
                                    className="h-8 text-xs"
                                    onClick={() => setFilterUse(f)}
                                >
                                    {f === "all" ? "전체" : f === "active" ? "사용 중" : "미사용"}
                                </Button>
                            ))}
                        </div>
                        <div className="relative">
                            <Search className="w-4 h-4 absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
                            <Input
                                placeholder="검색..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="pl-8 h-9 w-52 text-sm"
                            />
                        </div>
                        <Button
                            variant="outline"
                            size="sm"
                            className="h-9 gap-1.5 text-xs"
                            onClick={() => fetchDevices(true)}
                        >
                            <RefreshCw className="w-3.5 h-3.5" />
                            새로고침
                        </Button>
                    </div>
                </CardHeader>
                <CardContent className="pt-0">
                    {/* Desktop table */}
                    <div className="hidden md:block overflow-x-auto rounded-lg border border-gray-200">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="bg-gray-50 border-b border-gray-200">
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">SEQ</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">디바이스 ID</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">멤버 ID</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">시리얼 번호</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">타입</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">등록일</th>
                                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap">사용</th>
                                    <th className="px-4 py-3" />
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.length === 0 ? (
                                    <tr>
                                        <td colSpan={8} className="text-center py-10 text-gray-400">일치하는 디바이스가 없습니다</td>
                                    </tr>
                                ) : (
                                    filtered.map((device, idx) => (
                                        <tr
                                            key={device.seq}
                                            className={`border-b border-gray-100 last:border-0 hover:bg-blue-50/40 transition-colors cursor-pointer ${idx % 2 === 0 ? "bg-white" : "bg-gray-50/40"}`}
                                            onClick={() => setSelectedDevice(device)}
                                        >
                                            <td className="px-4 py-3 font-mono text-gray-500 text-xs">#{String(device.seq).padStart(4, "0")}</td>
                                            <td className="px-4 py-3"><span className="font-mono text-blue-700 font-medium">{device.deviceId}</span></td>
                                            <td className="px-4 py-3"><span className="font-mono text-gray-600">{device.memberId}</span></td>
                                            <td className="px-4 py-3"><span className="font-mono text-gray-500 text-xs">{device.serialNum}</span></td>
                                            <td className="px-4 py-3"><Badge variant="outline" className="font-mono text-xs">{device.deviceType}</Badge></td>
                                            <td className="px-4 py-3 text-gray-500 text-xs whitespace-nowrap">{new Date(device.regDate).toLocaleDateString("ko-KR")}</td>
                                            <td className="px-4 py-3">
                                                {device.isUse ? (
                                                    <Badge className="bg-green-100 text-green-700 border-green-200 hover:bg-green-100 text-xs">
                                                        <CheckCircle2 className="w-3 h-3 mr-1" />사용
                                                    </Badge>
                                                ) : (
                                                    <Badge variant="secondary" className="text-xs">
                                                        <XCircle className="w-3 h-3 mr-1" />미사용
                                                    </Badge>
                                                )}
                                            </td>
                                            <td className="px-4 py-3 text-right">
                                                <Button variant="ghost" size="icon" className="h-7 w-7" onClick={(e) => { e.stopPropagation(); setSelectedDevice(device); }}>
                                                    <ChevronRight className="w-4 h-4 text-gray-400" />
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>

                    {/* Mobile cards */}
                    <div className="md:hidden space-y-3">
                        {filtered.length === 0 ? (
                            <div className="text-center py-10 text-gray-400">일치하는 디바이스가 없습니다</div>
                        ) : (
                            filtered.map((device) => (
                                <div
                                    key={device.seq}
                                    className="border border-gray-200 rounded-xl p-4 bg-white hover:bg-blue-50/40 transition-colors cursor-pointer"
                                    onClick={() => setSelectedDevice(device)}
                                >
                                    <div className="flex items-start justify-between mb-3">
                                        <div>
                                            <div className="font-mono font-semibold text-blue-700">{device.deviceId}</div>
                                            <div className="text-xs text-gray-400 mt-0.5 font-mono">#{String(device.seq).padStart(4, "0")}</div>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            {device.isUse
                                                ? <Badge className="bg-green-100 text-green-700 border-green-200 hover:bg-green-100 text-xs">사용</Badge>
                                                : <Badge variant="secondary" className="text-xs">미사용</Badge>}
                                            <ChevronRight className="w-4 h-4 text-gray-400" />
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs text-gray-500">
                                        <span><span className="text-gray-400">멤버</span> {device.memberId}</span>
                                        <span><span className="text-gray-400">타입</span> {device.deviceType}</span>
                                        <span className="col-span-2"><span className="text-gray-400">S/N</span> {device.serialNum}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    {filtered.length > 0 && (
                        <p className="text-xs text-gray-400 mt-3 text-right">총 {filtered.length}개 디바이스 표시 중</p>
                    )}
                </CardContent>
            </Card>

            {/* Device Detail Dialog */}
            <Dialog open={!!selectedDevice} onOpenChange={(open) => !open && setSelectedDevice(null)}>
                <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            <Cpu className="w-5 h-5 text-blue-600" />
                            디바이스 상세 정보
                        </DialogTitle>
                    </DialogHeader>
                    {selectedDevice && (
                        <div className="space-y-3 pt-2">
                            <div className={`flex items-center gap-2 p-3 rounded-lg border ${selectedDevice.isUse ? "bg-green-50 border-green-200 text-green-700" : "bg-gray-100 border-gray-200 text-gray-500"
                                }`}>
                                {selectedDevice.isUse ? <CheckCircle2 className="w-4 h-4 flex-shrink-0" /> : <XCircle className="w-4 h-4 flex-shrink-0" />}
                                <span className="text-sm font-medium">현재 {selectedDevice.isUse ? "사용 중인" : "미사용"} 디바이스입니다</span>
                            </div>
                            <div className="rounded-xl border border-gray-200 overflow-hidden">
                                {FIELD_META.map((meta, idx) => {
                                    const Icon = meta.icon;
                                    const value = selectedDevice[meta.key];
                                    return (
                                        <div
                                            key={meta.key}
                                            className={`flex items-start gap-3 px-4 py-3 ${idx < FIELD_META.length - 1 ? "border-b border-gray-100" : ""} ${idx % 2 === 0 ? "bg-white" : "bg-gray-50/60"}`}
                                        >
                                            <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0 mt-0.5">
                                                <Icon className="w-3.5 h-3.5 text-blue-500" />
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-1.5 mb-0.5">
                                                    <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">{meta.label}</span>
                                                    <span className="text-xs text-gray-300 font-mono">{meta.key}</span>
                                                </div>
                                                <div className="text-sm">{formatDeviceValue(meta.key, value)}</div>
                                                <div className="text-xs text-gray-400 mt-0.5 flex items-center gap-1">
                                                    <Info className="w-3 h-3" />{meta.description}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                            <Button variant="outline" className="w-full" onClick={() => setSelectedDevice(null)}>닫기</Button>
                        </div>
                    )}
                </DialogContent>
            </Dialog>
        </div>
    );
}

// ─────────────────────────────────────────────
// Main Settings Page (Tabbed)
// ─────────────────────────────────────────────

type TabKey = "member" | "device";

const TABS: { key: TabKey; label: string; icon: React.ElementType }[] = [
    { key: "member", label: "회원 정보", icon: User },
    { key: "device", label: "디바이스 정보", icon: Cpu },
];

export function SettingsPage() {
    const [activeTab, setActiveTab] = useState<TabKey>("member");

    return (
        <div className="space-y-6">
            {/* Page header */}
            <div>
                <h2 className="text-3xl font-bold text-gray-900">설정</h2>
                <p className="text-gray-500 mt-2">회원 정보와 등록된 펫하우스 디바이스를 관리하세요</p>
            </div>

            {/* Tab bar */}
            <div className="flex gap-1 bg-gray-100 rounded-xl p-1 w-fit">
                {TABS.map(({ key, label, icon: Icon }) => (
                    <button
                        key={key}
                        onClick={() => setActiveTab(key)}
                        className={`flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium transition-all ${activeTab === key
                                ? "bg-white text-blue-700 shadow-sm"
                                : "text-gray-500 hover:text-gray-800"
                            }`}
                    >
                        <Icon className={`w-4 h-4 ${activeTab === key ? "text-blue-600" : "text-gray-400"}`} />
                        {label}
                    </button>
                ))}
            </div>

            {/* Tab content */}
            {activeTab === "member" ? <MemberTab /> : <DeviceTab />}
        </div>
    );
}
