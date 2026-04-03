package com.note.noteserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.note.noteserver.dto.ResolveConflictRequest;
import com.note.noteserver.dto.SyncRequest;
import com.note.noteserver.dto.SyncResponse;
import com.note.noteserver.dto.SyncStatusResponse;
import com.note.noteserver.entity.MoodEntry;
import com.note.noteserver.mapper.MoodEntryMapper;
import com.note.noteserver.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据同步服务实现类 - 以 SYNC_DESIGN.md 为准
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private static final Duration CONFLICT_WINDOW = Duration.ofMinutes(5);

    private final MoodEntryMapper moodEntryMapper;

    @Override
    @Transactional
    public SyncResponse sync(String userId, SyncRequest request) {
        LocalDateTime serverTime = LocalDateTime.now();
        LocalDateTime lastSyncAt = request.getLastSyncAt();

        ApplyResult apply = applyClientChanges(userId, request, lastSyncAt);

        List<SyncRequest.MoodEntryDto> serverEntries = getServerEntries(userId, lastSyncAt);
        List<SyncRequest.DeleteDto> serverDeletes = getServerDeletes(userId, lastSyncAt);

        return SyncResponse.builder()
                .serverTime(serverTime)
                .entries(serverEntries)
                .deletes(serverDeletes)
                .conflicts(apply.conflicts)
                .stats(SyncResponse.Stats.builder()
                        .uploaded(apply.uploaded)
                        .downloaded((serverEntries != null ? serverEntries.size() : 0) + (serverDeletes != null ? serverDeletes.size() : 0))
                        .deleted(apply.deleted)
                        .conflicts(apply.conflicts.size())
                        .build())
                .build();
    }


    @Override
    @Transactional
    public void resolveConflict(String userId, String entryId, ResolveConflictRequest request) {
        MoodEntry server = moodEntryMapper.selectById(entryId);
        if (server == null || !userId.equals(server.getUserId())) {
            throw new RuntimeException("entry not found");
        }

        String resolution = request.getResolution();
        if ("server".equalsIgnoreCase(resolution)) {
            return;
        }
        if ("client".equalsIgnoreCase(resolution)) {
            if (request.getEntry() == null) {
                throw new RuntimeException("entry is required when resolution=client");
            }
            if (!entryId.equals(request.getEntry().getId())) {
                throw new RuntimeException("path entryId and body entry.id mismatch");
            }
            saveOrUpdateEntry(userId, request.getEntry(), null);
            return;
        }

        throw new RuntimeException("invalid resolution");
    }

    private static final class ApplyResult {
        private int uploaded;
        private int deleted;
        private final List<SyncResponse.ConflictInfo> conflicts = new ArrayList<>();
    }

    private ApplyResult applyClientChanges(String userId, SyncRequest request, LocalDateTime lastSyncAt) {
        ApplyResult result = new ApplyResult();

        if (request.getEntries() != null) {
            for (SyncRequest.MoodEntryDto entry : request.getEntries()) {
                if (entry == null || entry.getId() == null) {
                    continue;
                }
                result.uploaded++;
                Conflict conflict = saveOrUpdateEntry(userId, entry, lastSyncAt);
                if (conflict != null) {
                    result.conflicts.add(conflict.toDto());
                }
            }
        }

        if (request.getDeletes() != null) {
            for (SyncRequest.DeleteDto del : request.getDeletes()) {
                if (del == null || del.getId() == null || del.getDeletedAt() == null) {
                    continue;
                }
                boolean applied = applyDelete(userId, del.getId(), del.getDeletedAt(), lastSyncAt);
                if (applied) {
                    result.deleted++;
                }
            }
        }

        return result;
    }

    private static final class Conflict {
        private final String id;
        private final LocalDateTime clientUpdatedAt;
        private final LocalDateTime serverUpdatedAt;
        private final SyncRequest.MoodEntryDto clientEntry;
        private final SyncRequest.MoodEntryDto serverEntry;
        private final String autoResolution;

        private Conflict(String id,
                         LocalDateTime clientUpdatedAt,
                         LocalDateTime serverUpdatedAt,
                         SyncRequest.MoodEntryDto clientEntry,
                         SyncRequest.MoodEntryDto serverEntry,
                         String autoResolution) {
            this.id = id;
            this.clientUpdatedAt = clientUpdatedAt;
            this.serverUpdatedAt = serverUpdatedAt;
            this.clientEntry = clientEntry;
            this.serverEntry = serverEntry;
            this.autoResolution = autoResolution;
        }

        private SyncResponse.ConflictInfo toDto() {
            return SyncResponse.ConflictInfo.builder()
                    .id(id)
                    .clientUpdatedAt(clientUpdatedAt)
                    .serverUpdatedAt(serverUpdatedAt)
                    .clientEntry(clientEntry)
                    .serverEntry(serverEntry)
                    .autoResolution(autoResolution)
                    .build();
        }
    }

    /**
     * @return Conflict if need manual resolution, otherwise null
     */
    private Conflict saveOrUpdateEntry(String userId, SyncRequest.MoodEntryDto dto, LocalDateTime lastSyncAt) {
        // tombstone
        if (dto.getDeletedAt() != null) {
            applyDelete(userId, dto.getId(), dto.getDeletedAt(), lastSyncAt);
            return null;
        }

        MoodEntry server = moodEntryMapper.selectById(dto.getId());
        if (server == null) {
            MoodEntry created = new MoodEntry();
            created.setId(dto.getId());
            created.setUserId(userId);
            created.setEntryDate(dto.getDate() != null ? LocalDate.parse(dto.getDate()) : null);
            created.setMoodType(dto.getMood());
            created.setJournalContent(dto.getJournal());
            created.setPhotos(toJsonArray(dto.getPhotos()));
            created.setFactors(toJsonArray(dto.getFactors()));
            created.setJournalEncrypted(false);
            created.setSyncVersion(1);
            created.setIsDeleted(false);
            created.setDeletedAt(null);
            if (dto.getCreatedAt() != null) {
                created.setCreatedAt(dto.getCreatedAt());
            }
            if (dto.getUpdatedAt() != null) {
                created.setUpdatedAt(dto.getUpdatedAt());
            }
            moodEntryMapper.insert(created);
            return null;
        }

        if (!userId.equals(server.getUserId())) {
            return null;
        }

        LocalDateTime clientUpdatedAt = dto.getUpdatedAt();
        LocalDateTime serverUpdatedAt = server.getUpdatedAt();

        if (lastSyncAt != null && clientUpdatedAt != null && serverUpdatedAt != null
                && clientUpdatedAt.isAfter(lastSyncAt) && serverUpdatedAt.isAfter(lastSyncAt)) {
            Duration delta = Duration.between(clientUpdatedAt, serverUpdatedAt).abs();
            if (delta.compareTo(CONFLICT_WINDOW) < 0) {
                return new Conflict(
                        dto.getId(),
                        clientUpdatedAt,
                        serverUpdatedAt,
                        dto,
                        convertToEntryDto(server),
                        "manual"
                );
            }
            // auto resolution by timestamp
            if (clientUpdatedAt.isAfter(serverUpdatedAt)) {
                updateServerEntryFromClient(server, dto);
                return null;
            }
            return null;
        }

        // timestamp wins
        if (clientUpdatedAt != null && serverUpdatedAt != null && serverUpdatedAt.isAfter(clientUpdatedAt)) {
            return null;
        }

        updateServerEntryFromClient(server, dto);
        return null;
    }

    private void updateServerEntryFromClient(MoodEntry server, SyncRequest.MoodEntryDto dto) {
        if (dto.getDate() != null) {
            server.setEntryDate(LocalDate.parse(dto.getDate()));
        }
        if (dto.getMood() != null) {
            server.setMoodType(dto.getMood());
        }
        if (dto.getJournal() != null) {
            server.setJournalContent(dto.getJournal());
        }
        if (dto.getPhotos() != null) {
            server.setPhotos(toJsonArray(dto.getPhotos()));
        }
        if (dto.getFactors() != null) {
            server.setFactors(toJsonArray(dto.getFactors()));
        }
        if (dto.getUpdatedAt() != null) {
            server.setUpdatedAt(dto.getUpdatedAt());
        }
        server.setIsDeleted(false);
        server.setDeletedAt(null);
        server.setSyncVersion(server.getSyncVersion() != null ? server.getSyncVersion() + 1 : 1);
        moodEntryMapper.updateById(server);

    }

    private boolean applyDelete(String userId, String entryId, LocalDateTime deletedAt, LocalDateTime lastSyncAt) {
        MoodEntry entry = moodEntryMapper.selectById(entryId);
        if (entry == null || !userId.equals(entry.getUserId())) {
            return false;
        }

        // conflict with server update after lastSyncAt
        if (lastSyncAt != null && entry.getUpdatedAt() != null
                && entry.getUpdatedAt().isAfter(lastSyncAt)
                && deletedAt != null && deletedAt.isAfter(lastSyncAt)) {
            if (!deletedAt.isAfter(entry.getUpdatedAt())) {
                return false;
            }
        }

        // 删除 vs 更新冲突：deletedAt > updatedAt => delete else keep
        if (entry.getUpdatedAt() != null && deletedAt != null && deletedAt.isBefore(entry.getUpdatedAt())) {
            return false;
        }

        // 幂等：只接受更晚的删除时间
        if (entry.getDeletedAt() != null && !deletedAt.isAfter(entry.getDeletedAt())) {
            return false;
        }

        entry.setIsDeleted(true);
        entry.setDeletedAt(deletedAt);
        entry.setSyncVersion(entry.getSyncVersion() != null ? entry.getSyncVersion() + 1 : 1);
        moodEntryMapper.updateById(entry);
        return true;
    }


    private List<SyncRequest.MoodEntryDto> getServerEntries(String userId, LocalDateTime lastSyncAt) {
        LambdaQueryWrapper<MoodEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MoodEntry::getUserId, userId)
                .eq(MoodEntry::getIsDeleted, false);

        if (lastSyncAt != null) {
            wrapper.gt(MoodEntry::getUpdatedAt, lastSyncAt);
        }

        List<MoodEntry> entries = moodEntryMapper.selectList(wrapper);
        return entries.stream().map(this::convertToEntryDto).collect(Collectors.toList());
    }

    private List<SyncRequest.DeleteDto> getServerDeletes(String userId, LocalDateTime lastSyncAt) {
        if (lastSyncAt == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<MoodEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MoodEntry::getUserId, userId)
                .eq(MoodEntry::getIsDeleted, true)
                .isNotNull(MoodEntry::getDeletedAt)
                .gt(MoodEntry::getDeletedAt, lastSyncAt);

        List<MoodEntry> deleted = moodEntryMapper.selectList(wrapper);
        return deleted.stream().map(e -> {
            SyncRequest.DeleteDto dto = new SyncRequest.DeleteDto();
            dto.setId(e.getId());
            dto.setDeletedAt(e.getDeletedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    private SyncRequest.MoodEntryDto convertToEntryDto(MoodEntry entry) {
        SyncRequest.MoodEntryDto dto = new SyncRequest.MoodEntryDto();
        dto.setId(entry.getId());
        dto.setDate(entry.getEntryDate() != null ? entry.getEntryDate().toString() : null);

        dto.setMood(entry.getMoodType());

        dto.setJournal(entry.getJournalContent());
        dto.setPhotos(fromJsonArray(entry.getPhotos()));
        dto.setFactors(fromJsonArray(entry.getFactors()));
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        dto.setDeletedAt(entry.getDeletedAt());
        return dto;
    }

    private List<String> fromJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank() || "[]".equals(jsonArray)) {
            return new ArrayList<>();
        }
        String inner = jsonArray.trim();
        if (inner.startsWith("[")) {
            inner = inner.substring(1);
        }
        if (inner.endsWith("]")) {
            inner = inner.substring(0, inner.length() - 1);
        }
        inner = inner.trim();
        if (inner.isEmpty()) {
            return new ArrayList<>();
        }
        // naive parse: assumes values are quoted and do not contain commas
        return Arrays.stream(inner.split(","))
                .map(s -> s.trim().replace("\"", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String toJsonArray(List<String> values) {
        if (values == null) {
            return "[]";
        }
        return "[" + values.stream()
                .filter(Objects::nonNull)
                .map(v -> "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

}
