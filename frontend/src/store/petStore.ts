import { create } from "zustand";

export interface PetHouse {
  id: string;
  name: string;
  petName: string;
  petType: "dog" | "cat" | "other";
  location: string;
  online: boolean;
  color: string; // tailwind color key
}

interface PetHouseState {
  houses: PetHouse[];
  activeHouseId: string;
  setActiveHouse: (house: PetHouse) => void;
  addHouse: (house: Omit<PetHouse, "id">) => void;
  removeHouse: (id: string) => void;
  updateHouse: (id: string, data: Partial<Omit<PetHouse, "id">>) => void;
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
  {
    id: "2",
    name: "2번 하우스",
    petName: "나비",
    petType: "cat",
    location: "안방",
    online: true,
    color: "purple",
  },
  {
    id: "3",
    name: "3번 하우스",
    petName: "망고",
    petType: "dog",
    location: "베란다",
    online: false,
    color: "orange",
  },
];

export const usePetStore = create<PetHouseState>((set, get) => ({
  houses: initialHouses,
  activeHouseId: initialHouses[0].id,

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
  }
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
