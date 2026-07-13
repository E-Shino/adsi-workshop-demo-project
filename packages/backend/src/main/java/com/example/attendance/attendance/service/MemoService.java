package com.example.attendance.attendance.service;

import com.example.attendance.attendance.domain.MemoType;
import com.example.attendance.attendance.dto.MemoRequest;
import com.example.attendance.attendance.dto.MemoResponse;

import java.util.UUID;

public interface MemoService {

    MemoResponse saveMemo(UUID attendanceRecordId, MemoType memoType, MemoRequest request, UUID requesterId);

    MemoResponse updateMemo(UUID attendanceRecordId, String memoType, MemoRequest request, UUID requesterId);

    void deleteMemo(UUID attendanceRecordId, String memoType, UUID requesterId);
}
