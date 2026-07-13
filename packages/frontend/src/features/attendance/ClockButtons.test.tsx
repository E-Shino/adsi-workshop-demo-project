import { describe, expect, it } from "vitest";
import { canPerformClockIn, canPerformClockOut } from "./ClockButtons";

describe("ClockButtons ボタン制御ロジック", () => {
  it("出勤済み(CLOCKED_IN)の場合、出勤ボタンは無効であるべき", () => {
    expect(canPerformClockIn("CLOCKED_IN")).toBe(false);
  });

  it("出勤済み(CLOCKED_IN)の場合、退勤ボタンは有効であるべき", () => {
    expect(canPerformClockOut("CLOCKED_IN")).toBe(true);
  });

  it("未出勤(NOT_CLOCKED_IN)の場合、出勤ボタンは有効であるべき", () => {
    expect(canPerformClockIn("NOT_CLOCKED_IN")).toBe(true);
  });

  it("退勤済み(CLOCKED_OUT)の場合、出勤ボタンは無効であるべき", () => {
    expect(canPerformClockIn("CLOCKED_OUT")).toBe(false);
  });

  it("退勤済み(CLOCKED_OUT)の場合、退勤ボタンは無効であるべき", () => {
    expect(canPerformClockOut("CLOCKED_OUT")).toBe(false);
  });
});
