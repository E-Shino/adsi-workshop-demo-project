import type { MemoCategory } from "./attendance-api";

export interface CategoryOption {
  value: MemoCategory;
  label: string;
}

const ALL_CATEGORIES: CategoryOption[] = [
  { value: "DIRECT_GO", label: "直行" },
  { value: "TRAIN_DELAY", label: "電車遅延" },
  { value: "REMOTE", label: "在宅" },
  { value: "INCIDENT", label: "障害対応" },
  { value: "OTHER", label: "その他" },
  { value: "DIRECT_RETURN", label: "直帰" },
  { value: "EARLY_LEAVE", label: "早退" },
  { value: "OUT_OF_OFFICE", label: "外出" },
];

const CLOCK_IN_VALUES: MemoCategory[] = [
  "DIRECT_GO",
  "TRAIN_DELAY",
  "REMOTE",
  "INCIDENT",
  "OTHER",
];

const CLOCK_OUT_VALUES: MemoCategory[] = [
  "DIRECT_RETURN",
  "EARLY_LEAVE",
  "OUT_OF_OFFICE",
  "REMOTE",
  "INCIDENT",
  "OTHER",
];

export function getCategoriesFor(
  memoType: "CLOCK_IN" | "CLOCK_OUT",
): CategoryOption[] {
  const values = memoType === "CLOCK_IN" ? CLOCK_IN_VALUES : CLOCK_OUT_VALUES;
  return ALL_CATEGORIES.filter((c) => values.includes(c.value));
}

export function getCategoryLabel(category: MemoCategory): string {
  return ALL_CATEGORIES.find((c) => c.value === category)?.label ?? category;
}
