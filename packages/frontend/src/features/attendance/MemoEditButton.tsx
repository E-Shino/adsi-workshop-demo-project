"use client";

import { Check, Pencil, X } from "lucide-react";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import type { AttendanceRecordResponse } from "./attendance-api";
import { useUpdateMemo } from "./useAttendance";

interface MemoEditButtonProps {
  record: AttendanceRecordResponse;
}

export function MemoEditButton({ record }: MemoEditButtonProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [clockInMemo, setClockInMemo] = useState(record.clockInMemo ?? "");
  const [clockOutMemo, setClockOutMemo] = useState(record.clockOutMemo ?? "");
  const updateMemo = useUpdateMemo();

  const handleSave = () => {
    updateMemo.mutate(
      {
        recordId: record.id,
        request: {
          clockInMemo,
          clockOutMemo,
          version: record.version,
        },
      },
      { onSuccess: () => setIsEditing(false) },
    );
  };

  const handleCancel = () => {
    setClockInMemo(record.clockInMemo ?? "");
    setClockOutMemo(record.clockOutMemo ?? "");
    setIsEditing(false);
  };

  if (!isEditing) {
    return (
      <button
        type="button"
        onClick={() => setIsEditing(true)}
        className="text-muted-foreground hover:text-foreground p-1 rounded"
        title="メモを編集"
      >
        <Pencil className="h-3 w-3" />
      </button>
    );
  }

  return (
    <div className="flex flex-col gap-1.5 min-w-[200px]">
      <Input
        type="text"
        placeholder="出勤メモ"
        value={clockInMemo}
        onChange={(e) => setClockInMemo(e.target.value)}
        maxLength={100}
        className="h-7 text-xs"
      />
      {record.clockOut && (
        <Input
          type="text"
          placeholder="退勤メモ"
          value={clockOutMemo}
          onChange={(e) => setClockOutMemo(e.target.value)}
          maxLength={100}
          className="h-7 text-xs"
        />
      )}
      <div className="flex gap-1">
        <button
          type="button"
          onClick={handleSave}
          disabled={updateMemo.isPending}
          className="text-green-600 hover:text-green-700 p-1 rounded"
          title="保存"
        >
          <Check className="h-3.5 w-3.5" />
        </button>
        <button
          type="button"
          onClick={handleCancel}
          className="text-muted-foreground hover:text-foreground p-1 rounded"
          title="キャンセル"
        >
          <X className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
}
