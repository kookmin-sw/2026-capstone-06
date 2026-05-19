import { create } from "zustand";
import apiClient from "../services/axios";

export interface PetHouse {
  id: string;
  name: string;
  petName: string;
  petType: "dog" | "cat" | "other";
  location: string;
  online: boolean;
  color: string; // tailwind color key
  serialNum?: string; // 기기의 실제 serialNum (차트/센서 API에 사용)
  deviceId?: string;  // 기기의 deviceId (센서 실시간 API에 사용)
}

interface PetHouseState {
  houses: PetHouse[];
  activeHouseId: string;
  isLoaded: boolean; // 백엔드에서 실제 데이터 로드 여부
  setActiveHouse: (house: PetHouse) => void;
  addHouse: (house: Omit<PetHouse, "id">) => void;
  removeHouse: (id: string) => void;
  updateHouse: (id: string, data: Partial<Omit<PetHouse, "id">>) => void;
  loadDevicesFromServer: (memberId: string) => Promise<void>;
  resetToDefault: () => void;
}

export const COLORS = [
  "blue", "purple", "green", "orange", "pink", "teal", "indigo", "rose",
];

export const COLOR_MAP: Record<string, { bg: string; border: string; text: string; light: string; dot: string }> = {
  blue:   { bg: "bg-blue-500",   border: "border-blue-400",   text: "text-blue-600",   light: "bg-blue-50",   dot: "bg-blue-500" },
  purple: { bg: "bg-purple-500", border: "border-purple-400", text: "text-purple-600", light: "bg-purple-50", dot: "bg-purple-500" },
  green:  { bg: "bg-green-500",  border: "border-green-400",  text: "text-green-600",  light: "bg-green-50",  dot: "bg-green-500" },
  orange: { bg: "bg-orange-500", border: "border-orange-400", text: "text-orange-600", light: "bg-orange-50", dot: "bg-orange-500" },
  pink:   { bg: "bg-pink-500",   border: "border-pink-400",   text: "text-pink-600",   light: "bg-pink-50",   dot: "bg-pink-500" },
  teal:   { bg: "bg-teal-500",   border: "border-teal-400",   text: "text-teal-600",   light: "bg-teal-50",   dot: "bg-teal-500" },
  indigo: { bg: "bg-indigo-500", border: "border-indigo-400", text: "text-indigo-600", light: "bg-indigo-50", dot: "bg-indigo-500" },
  rose:   { bg: "bg-rose-500",   border: "border-rose-400",   text: "text-rose-600",   light: "bg-rose-50",   dot: "bg-rose-500" },
};

export const PET_EMOJI: Record<string, string> = {
  dog: "🐶",
  cat: "🐱",
  other: "🐾",
};

const initialHouses: PetHouse[] = [
  {
    id: "1",
    name: "1번 하우스",
    petName: "초코",
    petType: "dog",
    location: "거실",
    online: true,
    color: "blue",
  },
];

export const usePetStore = create<PetHouseState>((set, get) => ({
  houses: initialHouses,
  activeHouseId: initialHouses[0].id,
  isLoaded: false,

  setActiveHouse: (house) => set({ activeHouseId: house.id }),

  addHouse: (data) => {
    const { houses } = get();
    const usedColors = houses.map((h) => h.color);
    const availableColor = COLORS.find((c) => !usedColors.includes(c)) ?? COLORS[houses.length % COLORS.length];
        
    const newHouse: PetHouse = {
        ...data,
        id: Date.now().toString(),
        color: availableColor,
    };

    set((state) => ({
      houses: [...state.houses, newHouse],
      activeHouseId: newHouse.id
    }));
  },

  removeHouse: (id) => {
    set((state) => {
      const remaining = state.houses.filter((h) => h.id !== id);
      const newActiveId = (state.activeHouseId === id && remaining.length > 0) 
        ? remaining[0].id 
        : state.activeHouseId;
      return {
        houses: remaining,
        activeHouseId: newActiveId
      };
    });
  },

  updateHouse: (id, data) => {
    set((state) => ({
      houses: state.houses.map((h) => (h.id === id ? { ...h, ...data } : h))
    }));
  },

  /**
   * 백엔드 GET /dashboard/devices?memberId=... API로 기기 목록을 불러와
   * petStore의 houses 상태를 실제 데이터로 교체합니다.
   */
  loadDevicesFromServer: async (memberId: string) => {
    try {
      const { data } = await apiClient.get<Array<{
        seq: number;
        deviceId: string;
        memberId: string;
        serialNum: string;
        deviceType: string;
        isUse: boolean;
        regDate: string;
      }>>('/dashboard/devices', { params: { memberId } });

      // deviceType === 'HOUSE'인 기기만 펫하우스로 등록
      const houseDevices = data.filter((d) => d.deviceType === 'HOUSE' && d.isUse);

      if (houseDevices.length === 0) {
        // 연결된 기기가 없으면 기본 더미 유지
        return;
      }

      const houses: PetHouse[] = houseDevices.map((d, index) => ({
        id: String(d.seq),
        name: `펫하우스 ${index + 1}`,
        petName: '-',
        petType: 'dog' as const,
        location: '-',
        online: true,
        color: COLORS[index % COLORS.length],
        serialNum: d.serialNum,
        deviceId: d.deviceId,
      }));

      set({
        houses,
        activeHouseId: houses[0].id,
        isLoaded: true,
      });
    } catch (error) {
      console.error('[petStore] 기기 목록 로드 실패:', error);
    }
  },

  resetToDefault: () =>
    set({ houses: initialHouses, activeHouseId: initialHouses[0].id, isLoaded: false }),
}));

// Provide the same hook signature for backwards compatibility
export const usePetHouse = () => {
  const store = usePetStore();
  const activeHouse = store.houses.find((h) => h.id === store.activeHouseId) ?? store.houses[0];
  
  return {
    ...store,
    activeHouse
  };
};
