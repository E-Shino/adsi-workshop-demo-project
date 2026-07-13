"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { MemoCategory, MemoRequest } from "./attendance-api";
import { getCategoriesFor } from "./memo-categories";

interface ClockMemoDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (memo?: MemoRequest) => void;
  type: "CLOCK_IN" | "CLOCK_OUT";
  isPending: boolean;
}

const MAX_NOTE_LENGTH = 200;

export function ClockMemoDialog({
  open,
  onClose,
  onConfirm,
  type,
  isPending,
}: ClockMemoDialogProps) {
  const [selectedCategory, setSelectedCategory] = useState<MemoCategory | null>(null);
  const [note, setNote] = useState("");

  const categories = getCategoriesFor(type);
  const title = type === "CLOCK_IN" ? "出勤打刻" : "退勤打刻";

  const isOtherSelected = selectedCategory === "OTHER";
  const isNoteRequiredAndMissing = isOtherSelected && note.trim() === "";
  const canConfirmWithMemo = selectedCategory !== null && !isNoteRequiredAndMissing;

  function handleConfirmWithMemo() {
    if (!selectedCategory) return;
    onConfirm({ category: selectedCategory, note: note.trim() });
    resetForm();
  }

  function handleSkip() {
    onConfirm(undefined);
    resetForm();
  }

  function handleClose() {
    onClose();
    resetForm();
  }

  function resetForm() {
    setSelectedCategory(null);
    setNote("");
  }

  return (
    <Dialog open={open} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <p className="text-sm font-medium mb-2">カテゴリ</p>
            <div className="flex flex-wrap gap-2">
              {categories.map((cat) => (
                <button
                  key={cat.value}
                  type="button"
                  onClick={() => setSelectedCategory(cat.value)}
                  className={`rounded-full px-3 py-1 text-sm border transition-colors ${
                    selectedCategory === cat.value
                      ? "bg-primary text-primary-foreground border-primary"
                      : "bg-background hover:bg-muted border-border"
                  }`}
                >
                  {cat.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="text-sm font-medium mb-1">メモ {isOtherSelected ? "(必須)" : "(任意)"}</p>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value.slice(0, MAX_NOTE_LENGTH))}
              placeholder="メモを入力..."
              rows={3}
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring resize-none"
            />
            <p className="text-xs text-muted-foreground text-right">
              {note.length}/{MAX_NOTE_LENGTH}文字
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleSkip} disabled={isPending}>
            メモなしで打刻
          </Button>
          <Button onClick={handleConfirmWithMemo} disabled={!canConfirmWithMemo || isPending}>
            {title}を確定
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
