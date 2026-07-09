"use client";

import { ChevronDown, ChevronRight } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { DailyAttendanceResponse } from "./attendance-api";
import { formatDate, formatMinutes, formatTime } from "./format";
import { MemoEditButton } from "./MemoEditButton";

function firstClockIn(day: DailyAttendanceResponse): string {
  const record = day.records[0];
  return record ? formatTime(record.clockIn) : "--:--";
}

function lastClockOut(day: DailyAttendanceResponse): string {
  const last = day.records[day.records.length - 1];
  return last?.clockOut ? formatTime(last.clockOut) : "--:--";
}

function hasCorrected(day: DailyAttendanceResponse): boolean {
  return day.records.some((r) => r.corrected);
}

function hasMemos(day: DailyAttendanceResponse): boolean {
  return day.records.some((r) => r.clockInMemo || r.clockOutMemo);
}

interface AttendanceTableProps {
  days: DailyAttendanceResponse[];
}

export function AttendanceTable({ days }: AttendanceTableProps) {
  const [expandedDate, setExpandedDate] = useState<string | null>(null);

  const toggleExpand = (date: string) => {
    setExpandedDate((prev) => (prev === date ? null : date));
  };

  if (days.length === 0) {
    return <p className="text-sm text-muted-foreground py-4">勤怠データがありません</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>日付</TableHead>
          <TableHead>出勤</TableHead>
          <TableHead>退勤</TableHead>
          <TableHead>勤務時間</TableHead>
          <TableHead>休憩</TableHead>
          <TableHead>残業</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {days.map((day) => {
          const expandable = hasMemos(day);
          const isExpanded = expandedDate === day.date;
          return (
            <TableRow key={day.date} className="group">
              <TableCell
                className={expandable ? "cursor-pointer select-none" : ""}
                onClick={() => expandable && toggleExpand(day.date)}
              >
                <div className="flex items-center gap-1">
                  {expandable &&
                    (isExpanded ? (
                      <ChevronDown className="h-3 w-3 text-muted-foreground" />
                    ) : (
                      <ChevronRight className="h-3 w-3 text-muted-foreground" />
                    ))}
                  {formatDate(day.date)}
                </div>
              </TableCell>
              <TableCell>{firstClockIn(day)}</TableCell>
              <TableCell>{lastClockOut(day)}</TableCell>
              <TableCell>{day.workMinutes > 0 ? formatMinutes(day.workMinutes) : "-"}</TableCell>
              <TableCell>{day.breakMinutes > 0 ? formatMinutes(day.breakMinutes) : "-"}</TableCell>
              <TableCell>
                {day.overtimeMinutes > 0 ? formatMinutes(day.overtimeMinutes) : "-"}
              </TableCell>
              <TableCell>{hasCorrected(day) && <Badge variant="outline">修正</Badge>}</TableCell>
            </TableRow>
          );
        })}
        {days.map((day) => {
          if (expandedDate !== day.date) return null;
          return (
            <TableRow key={`${day.date}-detail`} className="bg-muted/30 hover:bg-muted/30">
              <TableCell colSpan={7} className="px-8 py-3">
                <div className="space-y-2">
                  {day.records.map((record) => (
                    <div key={record.id} className="flex items-start justify-between gap-4">
                      <div className="text-xs space-y-0.5">
                        {record.clockInMemo && (
                          <p>
                            <span className="font-medium">出勤メモ:</span> {record.clockInMemo}
                          </p>
                        )}
                        {record.clockOutMemo && (
                          <p>
                            <span className="font-medium">退勤メモ:</span> {record.clockOutMemo}
                          </p>
                        )}
                        {!record.clockInMemo && !record.clockOutMemo && (
                          <p className="text-muted-foreground">メモなし</p>
                        )}
                      </div>
                      <MemoEditButton record={record} />
                    </div>
                  ))}
                </div>
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
