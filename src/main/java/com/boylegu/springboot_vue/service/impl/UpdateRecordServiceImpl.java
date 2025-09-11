// src/main/java/com/boylegu/springboot_vue/service/impl/UpdateRecordServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import com.boylegu.springboot_vue.entities.BaseEntity;
import com.boylegu.springboot_vue.repository.UpdateRecordRepository;
import com.boylegu.springboot_vue.service.UpdateRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 更新记录服务实现类
 */
@Service
public class UpdateRecordServiceImpl implements UpdateRecordService {
    
    @Autowired
    private UpdateRecordRepository updateRecordRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public UpdateRecordServiceImpl() {
        // 配置ObjectMapper以确保所有字段都被序列化，包括null值
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
    }
    
    /**
     * 记录实体创建操作
     */
    @Override
    public UpdateRecord logCreate(BaseEntity entity, String operator, String description) {
        UpdateRecord record = createUpdateRecord(
            entity.getClass().getSimpleName(),
            entity.getId(),
            entity.get__name__(),
            UpdateRecord.OperationType.CREATE,
            null,
            entityToString(entity),
            operator,
            description
        );
        
        return updateRecordRepository.save(record);
    }
    
    /**
     * 记录实体更新操作
     */
    @Override
    public UpdateRecord logUpdate(BaseEntity oldEntity, BaseEntity newEntity, String operator, String description) {
        UpdateRecord record = createUpdateRecord(
            newEntity.getClass().getSimpleName(),
            newEntity.getId(),
            newEntity.get__name__(),
            UpdateRecord.OperationType.UPDATE,
            entityToString(oldEntity),
            entityToString(newEntity),
            operator,
            description
        );
        
        return updateRecordRepository.save(record);
    }
    
    /**
     * 记录实体删除操作
     */
    @Override
    public UpdateRecord logDelete(BaseEntity entity, String operator, String description) {
        UpdateRecord record = createUpdateRecord(
            entity.getClass().getSimpleName(),
            entity.getId(),
            entity.get__name__(),
            UpdateRecord.OperationType.DELETE,
            entityToString(entity),
            null,
            operator,
            description
        );
        
        return updateRecordRepository.save(record);
    }
    
    /**
     * 创建更新记录
     */
    private UpdateRecord createUpdateRecord(String entityType, Long entityId, String entityName,
                                          UpdateRecord.OperationType operationType,
                                          String beforeData, String afterData,
                                          String operator, String description) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setEntityName(entityName);
        record.setOperationType(operationType);
        record.setBeforeData(beforeData);
        record.setAfterData(afterData);
        record.setOperator(operator);
        record.setDescription(description);
        return record;
    }
    
    /**
     * 将实体转换为字符串（用于存储）
     */
    private String entityToString(BaseEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            return "Error serializing entity: " + e.getMessage();
        }
    }
}