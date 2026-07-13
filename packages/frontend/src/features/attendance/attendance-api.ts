import { apiClient } from "@/lib/api-client";

export type MemoCategory =
  | "DIRECT_GO"
  | "TRAIN_DELAY"
  | "REMOTE"
  | "INCIDENT"
  | "OTHER"
  | "DIRECT_RETURN"
  | "EARLY_LEAVE"
  | "OUT_OF_OFFICE";

export interface MemoResponse {
  id: string;
  category: MemoCategory;
  categoryLabel: string;
  note: string | null;
}

export interface MemoRequest {
  category: MemoCategory;
  note: string;
}

export interface AttendanceRecordResponse {
  id: string;
  workDate: string;
  clockIn: string;
  clockOut: string | null;
  corrected: boolean;
  clockInMemo: MemoResponse | null;
  clockOutMemo: MemoResponse | null;
}

export interface TodayStatusResponse {
  status: "NOT_CLOCKED_IN" | "CLOCKED_IN" | "CLOCKED_OUT";
  records: AttendanceRecordResponse[];
}

export interface DailyAttendanceResponse {
  date: string;
  records: AttendanceRecordResponse[];
  totalWorkMinutes: number;
  breakMinutes: number;
  workMinutes: number;
  overtimeMinutes: number;
}

export interface MonthlySummaryResponse {
  workDays: number;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  absentDays: number;
}

export interface AttendanceHistoryResponse {
  month: string;
  days: DailyAttendanceResponse[];
  summary: MonthlySummaryResponse;
}

export interface TeamMemberSummaryResponse {
  employeeId: string;
  employeeName: string;
  workDays: number;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  absentDays: number;
}

export function clockIn(
  employeeId: string,
  memo?: MemoRequest,
): Promise<AttendanceRecordResponse> {
  const body = memo ? { memo } : undefined;
  return apiClient.post<AttendanceRecordResponse>(
    `/api/attendance/clock-in?employeeId=${employeeId}`,
    body,
  );
}

export function clockOut(
  employeeId: string,
  memo?: MemoRequest,
): Promise<AttendanceRecordResponse> {
  const body = memo ? { memo } : undefined;
  return apiClient.post<AttendanceRecordResponse>(
    `/api/attendance/clock-out?employeeId=${employeeId}`,
    body,
  );
}

export function updateMemo(
  recordId: string,
  memoType: "CLOCK_IN" | "CLOCK_OUT",
  employeeId: string,
  request: MemoRequest,
): Promise<MemoResponse> {
  return apiClient.put<MemoResponse>(
    `/api/attendance/${recordId}/memo/${memoType}?employeeId=${employeeId}`,
    request,
  );
}

export function deleteMemo(
  recordId: string,
  memoType: "CLOCK_IN" | "CLOCK_OUT",
  employeeId: string,
): Promise<void> {
  return apiClient.delete<void>(
    `/api/attendance/${recordId}/memo/${memoType}?employeeId=${employeeId}`,
  );
}

export function fetchTodayStatus(employeeId: string): Promise<TodayStatusResponse> {
  return apiClient.get<TodayStatusResponse>(`/api/attendance/today?employeeId=${employeeId}`);
}

export function fetchHistory(
  employeeId: string,
  month: string,
): Promise<AttendanceHistoryResponse> {
  return apiClient.get<AttendanceHistoryResponse>(
    `/api/attendance/history?employeeId=${employeeId}&month=${month}`,
  );
}

export function fetchTeamAttendance(
  managerId: string,
  month: string,
): Promise<TeamMemberSummaryResponse[]> {
  return apiClient.get<TeamMemberSummaryResponse[]>(
    `/api/attendance/team?managerId=${managerId}&month=${month}`,
  );
}

export function fetchAllAttendance(
  month: string,
  departmentId?: string,
): Promise<TeamMemberSummaryResponse[]> {
  const params = new URLSearchParams({ month });
  if (departmentId) {
    params.set("departmentId", departmentId);
  }
  return apiClient.get<TeamMemberSummaryResponse[]>(`/api/attendance/all?${params.toString()}`);
}
