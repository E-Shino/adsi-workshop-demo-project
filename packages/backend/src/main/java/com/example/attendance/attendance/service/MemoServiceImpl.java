package com.example.attendance.attendance.service;

import com.example.attendance.attendance.domain.MemoCategory;
import com.example.attendance.attendance.domain.MemoType;
import com.example.attendance.attendance.dto.MemoRequest;
import com.example.attendance.attendance.dto.MemoResponse;
import com.example.attendance.attendance.entity.AttendanceMemo;
import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.attendance.repository.AttendanceMemoRepository;
import com.example.attendance.attendance.repository.AttendanceRecordRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemoServiceImpl implements MemoService {

    private final AttendanceMemoRepository memoRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public MemoServiceImpl(AttendanceMemoRepository memoRepository,
                           AttendanceRecordRepository attendanceRecordRepository) {
        this.memoRepository = memoRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    @Transactional
    public MemoResponse saveMemo(UUID attendanceRecordId, MemoType memoType, MemoRequest request, UUID requesterId) {
        var record = findRecordOrThrow(attendanceRecordId);
        verifyOwnership(record, requesterId);
        validateMemoRequest(memoType, request);

        memoRepository.findByAttendanceRecordIdAndMemoType(attendanceRecordId, memoType)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Memo already exists for this type");
                });

        var memo = AttendanceMemo.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .attendanceRecord(record)
                .memoType(memoType)
                .category(request.category())
                .note(request.note())
                .build();

        var saved = memoRepository.save(memo);
        log.info("Memo saved for record={} type={}", attendanceRecordId, memoType);
        return MemoResponse.from(saved);
    }

    @Override
    @Transactional
    public MemoResponse updateMemo(UUID attendanceRecordId, String memoTypeStr, MemoRequest request, UUID requesterId) {
        var memoType = parseMemoType(memoTypeStr);
        var record = findRecordOrThrow(attendanceRecordId);
        verifyOwnership(record, requesterId);
        validateMemoRequest(memoType, request);

        var memo = memoRepository.findByAttendanceRecordIdAndMemoType(attendanceRecordId, memoType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Memo not found"));

        memo.setCategory(request.category());
        memo.setNote(request.note());

        var saved = memoRepository.save(memo);
        log.info("Memo updated for record={} type={}", attendanceRecordId, memoType);
        return MemoResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteMemo(UUID attendanceRecordId, String memoTypeStr, UUID requesterId) {
        var memoType = parseMemoType(memoTypeStr);
        var record = findRecordOrThrow(attendanceRecordId);
        verifyOwnership(record, requesterId);

        var memo = memoRepository.findByAttendanceRecordIdAndMemoType(attendanceRecordId, memoType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Memo not found"));

        memoRepository.delete(memo);
        log.info("Memo deleted for record={} type={}", attendanceRecordId, memoType);
    }

    private MemoType parseMemoType(String memoTypeStr) {
        try {
            return MemoType.valueOf(memoTypeStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid memo type: " + memoTypeStr);
        }
    }

    private AttendanceRecord findRecordOrThrow(UUID attendanceRecordId) {
        return attendanceRecordRepository.findById(attendanceRecordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));
    }

    private void verifyOwnership(AttendanceRecord record, UUID requesterId) {
        if (!record.getEmployee().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another employee's memo");
        }
    }

    private void validateMemoRequest(MemoType memoType, MemoRequest request) {
        if (!request.category().isValidFor(memoType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category '%s' is not valid for %s".formatted(request.category(), memoType));
        }
        if (request.category() == MemoCategory.OTHER &&
                (request.note() == null || request.note().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Note is required when category is OTHER");
        }
    }
}
