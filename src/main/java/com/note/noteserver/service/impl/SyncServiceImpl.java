package com.note.noteserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.note.noteserver.dto.*;
import com.note.noteserver.entity.*;
import com.note.noteserver.mapper.*;
import com.note.noteserver.service.SyncService;
import com.note.noteserver.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据同步服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final MoodEntryMapper moodEntryMapper;
    private final FactorOptionMapper factorOptionMapper;
    private final JournalTemplateMapper journalTemplateMapper;
    private final UserSettingMapper userSettingMapper;
    private final SyncStatusMapper syncStatusMapper;
    private final EntryFactorMapper entryFactorMapper;
    private final SecuritySettingMapper securitySettingMapper;

    @Override
    @Transactional
    public SyncResponse sync(String userId, SyncRequest request) {
        log.info("执行数据同步, 用户: {}, 设备: {}", userId, request.getDeviceId());
        
        LocalDateTime serverTimestamp = LocalDateTime.now();
        LocalDateTime lastSyncAt = request.getLastSyncAt();
        
        // 1. 处理客户端上传的数据
        processClientData(userId, request.getData(), request.getDeletedIds());
        
        // 2. 获取服务器端更新的数据
        SyncResponse.SyncResponseBuilder responseBuilder = SyncResponse.builder()
                .serverTimestamp(serverTimestamp);
        
        // 获取更新的日记条目
        List<SyncRequest.MoodEntryDto> serverEntries = getServerEntries(userId, lastSyncAt);
        responseBuilder.entries(serverEntries);
        
        // 获取用户设置
        SyncRequest.UserSettingsDto serverSettings = getServerSettings(userId);
        responseBuilder.settings(serverSettings);
        
        // 获取自定义因素
        List<SyncRequest.FactorOptionDto> serverFactors = getServerFactors(userId, lastSyncAt);
        responseBuilder.customFactors(serverFactors);
        
        // 获取自定义模板
        List<SyncRequest.JournalTemplateDto> serverTemplates = getServerTemplates(userId, lastSyncAt);
        responseBuilder.customTemplates(serverTemplates);

        // 获取安全设置
        SyncRequest.SecuritySettingsDto serverSecurity = getServerSecurity(userId);
        responseBuilder.securitySettings(serverSecurity);
        
        // 3. 检测冲突
        List<SyncResponse.ConflictInfo> conflicts = detectConflicts(userId, request.getData(), lastSyncAt);
        responseBuilder.conflicts(conflicts);
        
        // 4. 更新同步状态
        updateSyncStatus(userId, request.getDeviceId(), serverTimestamp);
        
        return responseBuilder.build();
    }

    @Override
    public SyncStatusResponse getSyncStatus(String userId) {
        log.info("获取同步状态, 用户: {}", userId);
        
        LambdaQueryWrapper<SyncStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncStatus::getUserId, userId)
               .eq(SyncStatus::getIsActive, true)
               .orderByDesc(SyncStatus::getLastSyncAt)
               .last("LIMIT 1");
        
        SyncStatus syncStatus = syncStatusMapper.selectOne(wrapper);
        
        if (syncStatus == null) {
            return SyncStatusResponse.builder()
                    .lastSyncAt(null)
                    .pendingChanges(0)
                    .build();
        }
        
        return SyncStatusResponse.builder()
                .lastSyncAt(syncStatus.getLastSyncAt())
                .pendingChanges(syncStatus.getPendingChanges())
                .build();
    }

    @Override
    @Transactional
    public void resolveConflict(String userId, ResolveConflictRequest request) {
        log.info("解决同步冲突, 用户: {}, 条目: {}, 方式: {}", 
                userId, request.getEntryId(), request.getResolution());
        
        String entryId = request.getEntryId();
        String resolution = request.getResolution();
        
        MoodEntry entry = moodEntryMapper.selectById(entryId);
        if (entry == null || !entry.getUserId().equals(userId)) {
            throw new RuntimeException("条目不存在或无权访问");
        }
        
        switch (resolution) {
            case "server":
                // 保留服务器版本，无需操作
                log.info("冲突解决: 保留服务器版本, 条目: {}", entryId);
                break;
            case "client":
                // 保留客户端版本（已在 sync 中处理）
                log.info("冲突解决: 保留客户端版本, 条目: {}", entryId);
                break;
            case "merged":
                // 使用合并后的版本
                if (request.getMergedEntry() == null) {
                    throw new RuntimeException("合并后的条目不能为空");
                }
                updateEntryFromDto(entry, request.getMergedEntry());
                entry.setUpdatedAt(LocalDateTime.now());
                moodEntryMapper.updateById(entry);
                log.info("冲突解决: 使用合并版本, 条目: {}", entryId);
                break;
            default:
                throw new RuntimeException("无效的解决方式: " + resolution);
        }
    }

    /**
     * 处理客户端上传的数据
     */
    private void processClientData(String userId, SyncRequest.SyncData data, List<String> deletedIds) {
        if (data == null) {
            return;
        }
        
        // 处理日记条目
        if (data.getEntries() != null) {
            for (SyncRequest.MoodEntryDto entryDto : data.getEntries()) {
                saveOrUpdateEntry(userId, entryDto);
            }
        }
        
        // 处理自定义因素
        if (data.getCustomFactors() != null) {
            for (SyncRequest.FactorOptionDto factorDto : data.getCustomFactors()) {
                saveOrUpdateFactor(userId, factorDto);
            }
        }
        
        // 处理自定义模板
        if (data.getCustomTemplates() != null) {
            for (SyncRequest.JournalTemplateDto templateDto : data.getCustomTemplates()) {
                saveOrUpdateTemplate(userId, templateDto);
            }
        }
        
        // 处理用户设置
        if (data.getSettings() != null) {
            saveOrUpdateSettings(userId, data.getSettings());
        }

        // 处理安全设置
        if (data.getSecuritySettings() != null) {
            saveOrUpdateSecuritySettings(userId, data.getSecuritySettings());
        }

        // 处理删除的条目
        if (deletedIds != null) {
            for (String entryId : deletedIds) {
                MoodEntry entry = moodEntryMapper.selectById(entryId);
                if (entry != null && entry.getUserId().equals(userId)) {
                    entry.setIsDeleted(true);
                    entry.setDeletedAt(LocalDateTime.now());
                    moodEntryMapper.updateById(entry);
                }
            }
        }
    }

    /**
     * 保存或更新日记条目
     */
    private void saveOrUpdateEntry(String userId, SyncRequest.MoodEntryDto dto) {
        MoodEntry entry = moodEntryMapper.selectById(dto.getId());
        
        if (entry == null) {
            // 新建条目
            entry = new MoodEntry();
            entry.setId(dto.getId());
            entry.setUserId(userId);
            entry.setEntryDate(LocalDate.parse(dto.getDate()));
            entry.setMoodType(dto.getMood());
            entry.setJournalContent(dto.getJournal());
            entry.setPhotos(dto.getPhotos() != null ? "[" + dto.getPhotos().stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(",")) + "]" : "[]");
            entry.setJournalEncrypted(false);
            entry.setSyncVersion(1);
            entry.setIsDeleted(false);
            moodEntryMapper.insert(entry);
            
            // 保存影响因素关联
            saveEntryFactors(entry.getId(), dto.getFactors());
        } else {
            // 更新条目
            updateEntryFromDto(entry, dto);
            entry.setSyncVersion(entry.getSyncVersion() + 1);
            moodEntryMapper.updateById(entry);
        }
    }

    /**
     * 更新条目数据
     */
    private void updateEntryFromDto(MoodEntry entry, SyncRequest.MoodEntryDto dto) {
        if (dto.getMood() != null) {
            entry.setMoodType(dto.getMood());
        }
        if (dto.getJournal() != null) {
            entry.setJournalContent(dto.getJournal());
        }
        if (dto.getPhotos() != null) {
            entry.setPhotos("[" + dto.getPhotos().stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(",")) + "]");
        }
    }

    /**
     * 保存影响因素关联
     */
    private void saveEntryFactors(String entryId, List<String> factorIds) {
        if (factorIds == null) {
            return;
        }
        
        // 删除旧关联
        entryFactorMapper.deleteByEntryId(entryId);
        
        // 添加新关联
        for (String factorId : factorIds) {
            EntryFactor entryFactor = new EntryFactor();
            entryFactor.setId(UUID.randomUUID().toString());
            entryFactor.setEntryId(entryId);
            entryFactor.setFactorId(factorId);
            entryFactorMapper.insert(entryFactor);
        }
    }

    /**
     * 保存或更新因素选项
     */
    private void saveOrUpdateFactor(String userId, SyncRequest.FactorOptionDto dto) {
        FactorOption factor = factorOptionMapper.selectById(dto.getId());
        
        if (factor == null) {
            factor = new FactorOption();
            factor.setId(dto.getId());
            factor.setUserId(userId);
            factor.setLabel(dto.getLabel());
            factor.setEmoji(dto.getEmoji());
            factor.setIsCustom(true);
            factor.setIsActive(true);
            factorOptionMapper.insert(factor);
        } else {
            factor.setLabel(dto.getLabel());
            factor.setEmoji(dto.getEmoji());
            factorOptionMapper.updateById(factor);
        }
    }

    /**
     * 保存或更新模板
     */
    private void saveOrUpdateTemplate(String userId, SyncRequest.JournalTemplateDto dto) {
        JournalTemplate template = journalTemplateMapper.selectById(dto.getId());
        
        if (template == null) {
            template = new JournalTemplate();
            template.setId(dto.getId());
            template.setUserId(userId);
            template.setCategory(dto.getCategory());
            template.setTitleKey(dto.getTitleKey());
            template.setContentKey(dto.getContentKey());
            template.setIsCustom(true);
            template.setIsActive(true);
            template.setUsageCount(0);
            journalTemplateMapper.insert(template);
        } else {
            template.setCategory(dto.getCategory());
            template.setTitleKey(dto.getTitleKey());
            template.setContentKey(dto.getContentKey());
            journalTemplateMapper.updateById(template);
        }
    }

    /**
     * 保存或更新用户设置
     */
    private void saveOrUpdateSettings(String userId, SyncRequest.UserSettingsDto dto) {
        UserSetting setting = userSettingMapper.findByUserId(userId);

        if (setting == null) {
            setting = new UserSetting();
            setting.setId(UUID.randomUUID().toString());
            setting.setUserId(userId);
            setting.setSettingsData("{}");
            setting.setEncrypted(dto.getEncrypted());
            userSettingMapper.insert(setting);
        } else {
            setting.setEncrypted(dto.getEncrypted());
            userSettingMapper.updateById(setting);
        }
    }

    /**
     * 保存或更新安全设置
     */
    private void saveOrUpdateSecuritySettings(String userId, SyncRequest.SecuritySettingsDto dto) {
        // 安全设置处理逻辑
        log.info("保存安全设置, 用户: {}, 密码启用: {}", userId, dto.getPasswordEnabled());
        SecuritySetting setting = securitySettingMapper.findByUserId(userId);
        if (setting == null) {
            setting = new SecuritySetting();
            setting.setId(UUID.randomUUID().toString());
            setting.setUserId(userId);
            setting.setPasswordProtected(dto.getPasswordEnabled());
            setting.setPasswordHash(dto.getPasswordHash());
            setting.setSecurityQuestions(dto.getSecurityQuestions() != null ? convertSecurityQuestionsToJson(dto.getSecurityQuestions()) : "[]");
            securitySettingMapper.insert(setting);
        } else {
            setting.setPasswordProtected(dto.getPasswordEnabled());
            setting.setSecurityQuestions(dto.getSecurityQuestions() != null ? convertSecurityQuestionsToJson(dto.getSecurityQuestions()) : "[]");
            setting.setPasswordHash(dto.getPasswordHash());
            securitySettingMapper.updateById(setting);
        }
    }

    /**
     * 将安全问题列表转换为 JSON 字符串
     */
    private String convertSecurityQuestionsToJson(List<SyncRequest.SecurityQuestionDto> questions) {
        if (questions == null || questions.isEmpty()) {
            return "[]";
        }
        return JsonUtil.toJsonArray(questions, q -> {
            Map<String, String> fields = new HashMap<>();
            fields.put("id", q.getId() != null ? q.getId() : "");
            fields.put("question", q.getQuestion() != null ? q.getQuestion() : "");
            fields.put("answerHash", q.getAnswerHash() != null ? q.getAnswerHash() : "");
            return JsonUtil.createJsonObject(fields);
        });
    }

    /**
     * 获取服务器端安全设置
     */
    private SyncRequest.SecuritySettingsDto getServerSecurity(String userId) {
        SecuritySetting setting = securitySettingMapper.findByUserId(userId);
        if (setting == null) {
            return null;
        }
        SyncRequest.SecuritySettingsDto dto = new SyncRequest.SecuritySettingsDto();
        dto.setPasswordEnabled(setting.getPasswordProtected());
        dto.setPasswordHash(setting.getPasswordHash());
        dto.setSecurityQuestions(JsonUtil.jsonToObject(setting.getSecurityQuestions(), List.class));
        return dto;
    }

    /**
     * 获取服务器端更新的日记条目
     */
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

    /**
     * 转换为 MoodEntryDto
     */
    private SyncRequest.MoodEntryDto convertToEntryDto(MoodEntry entry) {
        SyncRequest.MoodEntryDto dto = new SyncRequest.MoodEntryDto();
        dto.setId(entry.getId());
        dto.setDate(entry.getEntryDate().toString());
        dto.setMood(entry.getMoodType());
        dto.setJournal(entry.getJournalContent());
        dto.setPhotos(entry.getPhotos() != null && !entry.getPhotos().isEmpty() && !"[]".equals(entry.getPhotos()) ? 
                Arrays.asList(entry.getPhotos().replace("[", "").replace("]", "").replace("\"", "").split(",")) : 
                new ArrayList<>());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        
        // 获取影响因素
        List<EntryFactor> entryFactors = entryFactorMapper.findByEntryId(entry.getId());
        List<String> factorIds = entryFactors.stream()
                .map(EntryFactor::getFactorId)
                .collect(Collectors.toList());
        dto.setFactors(factorIds);
        
        return dto;
    }

    /**
     * 获取服务器端用户设置
     */
    private SyncRequest.UserSettingsDto getServerSettings(String userId) {
        UserSetting setting = userSettingMapper.findByUserId(userId);
        
        if (setting == null) {
            return null;
        }
        
        SyncRequest.UserSettingsDto dto = new SyncRequest.UserSettingsDto();
        dto.setEncrypted(setting.getEncrypted());
        dto.setCreatedAt(setting.getCreatedAt());
        return dto;
    }

    /**
     * 获取服务器端自定义因素
     */
    private List<SyncRequest.FactorOptionDto> getServerFactors(String userId, LocalDateTime lastSyncAt) {
        LambdaQueryWrapper<FactorOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FactorOption::getUserId, userId)
               .eq(FactorOption::getIsActive, true);
        
        if (lastSyncAt != null) {
            wrapper.gt(FactorOption::getUpdatedAt, lastSyncAt);
        }
        
        List<FactorOption> factors = factorOptionMapper.selectList(wrapper);
        
        return factors.stream().map(this::convertToFactorDto).collect(Collectors.toList());
    }

    /**
     * 转换为 FactorOptionDto
     */
    private SyncRequest.FactorOptionDto convertToFactorDto(FactorOption factor) {
        SyncRequest.FactorOptionDto dto = new SyncRequest.FactorOptionDto();
        dto.setId(factor.getId());
        dto.setLabel(factor.getLabel());
        dto.setEmoji(factor.getEmoji());
        dto.setIsCustom(factor.getIsCustom());
        return dto;
    }

    /**
     * 获取服务器端自定义模板
     */
    private List<SyncRequest.JournalTemplateDto> getServerTemplates(String userId, LocalDateTime lastSyncAt) {
        LambdaQueryWrapper<JournalTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JournalTemplate::getUserId, userId)
               .eq(JournalTemplate::getIsActive, true);
        
        if (lastSyncAt != null) {
            wrapper.gt(JournalTemplate::getUpdatedAt, lastSyncAt);
        }
        
        List<JournalTemplate> templates = journalTemplateMapper.selectList(wrapper);
        
        return templates.stream().map(this::convertToTemplateDto).collect(Collectors.toList());
    }

    /**
     * 转换为 JournalTemplateDto
     */
    private SyncRequest.JournalTemplateDto convertToTemplateDto(JournalTemplate template) {
        SyncRequest.JournalTemplateDto dto = new SyncRequest.JournalTemplateDto();
        dto.setId(template.getId());
        dto.setCategory(template.getCategory());
        dto.setTitleKey(template.getTitleKey());
        dto.setContentKey(template.getContentKey());
        dto.setIsCustom(template.getIsCustom());
        dto.setCreatedAt(template.getCreatedAt());
        return dto;
    }

    /**
     * 检测冲突
     */
    private List<SyncResponse.ConflictInfo> detectConflicts(String userId, 
            SyncRequest.SyncData clientData, LocalDateTime lastSyncAt) {
        List<SyncResponse.ConflictInfo> conflicts = new ArrayList<>();
        
        if (clientData == null || clientData.getEntries() == null || lastSyncAt == null) {
            return conflicts;
        }
        
        for (SyncRequest.MoodEntryDto clientEntry : clientData.getEntries()) {
            MoodEntry serverEntry = moodEntryMapper.selectById(clientEntry.getId());
            
            if (serverEntry != null && serverEntry.getUpdatedAt().isAfter(lastSyncAt)) {
                // 检测到冲突
                SyncResponse.ConflictInfo conflict = SyncResponse.ConflictInfo.builder()
                        .entryId(clientEntry.getId())
                        .resolution("server")  // 默认保留服务器版本
                        .build();
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }

    /**
     * 更新同步状态
     */
    private void updateSyncStatus(String userId, String deviceId, LocalDateTime syncTime) {
        SyncStatus syncStatus = syncStatusMapper.findByUserIdAndDeviceId(userId, deviceId);
        
        if (syncStatus == null) {
            syncStatus = new SyncStatus();
            syncStatus.setId(UUID.randomUUID().toString());
            syncStatus.setUserId(userId);
            syncStatus.setDeviceId(deviceId);
            syncStatus.setIsActive(true);
            syncStatus.setPendingChanges(0);
        }
        
        syncStatus.setLastSyncAt(syncTime);
        
        if (syncStatus.getId() == null) {
            syncStatusMapper.insert(syncStatus);
        } else {
            syncStatusMapper.updateById(syncStatus);
        }
    }
}
