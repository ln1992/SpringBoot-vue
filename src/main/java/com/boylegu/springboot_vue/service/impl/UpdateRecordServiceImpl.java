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

import java.util.List;

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
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType(entity.getClass().getSimpleName());
            record.setEntityId(entity.getId());
            record.setEntityName(entity.get__name__());
            record.setOperationType(UpdateRecord.OperationType.CREATE);
            record.setOperator(operator);
            record.setDescription(description);
            
            // 序列化新实体作为afterData
            String afterData = objectMapper.writeValueAsString(entity);
            record.setAfterData(afterData);
            
            return updateRecordRepository.save(record);
        } catch (Exception e) {
            // 记录日志但不中断主流程
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 记录实体更新操作（完整实体）
     */
    @Override
    public UpdateRecord logUpdate(BaseEntity oldEntity, BaseEntity newEntity, String operator, String description) {
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType(newEntity.getClass().getSimpleName());
            record.setEntityId(newEntity.getId());
            record.setEntityName(newEntity.get__name__());
            record.setOperationType(UpdateRecord.OperationType.UPDATE);
            record.setOperator(operator);
            record.setDescription(description);
            
            // 序列化旧实体作为beforeData
            String beforeData = objectMapper.writeValueAsString(oldEntity);
            record.setBeforeData(beforeData);
            
            // 序列化新实体作为afterData
            String afterData = objectMapper.writeValueAsString(newEntity);
            record.setAfterData(afterData);
            
            return updateRecordRepository.save(record);
        } catch (Exception e) {
            // 记录日志但不中断主流程
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 记录实体更新操作（仅变更字段）
     */
    public UpdateRecord logUpdate(String entityType, Long entityId, String entityName,
                                java.util.Map<String, Object> oldValues, java.util.Map<String, Object> newValues,
                                String operator, String description) {
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType(entityType);
            record.setEntityId(entityId);
            record.setEntityName(entityName);
            record.setOperationType(UpdateRecord.OperationType.UPDATE);
            record.setOperator(operator);
            record.setDescription(description);
            
            // 创建变更详情对象
            java.util.Map<String, Object> changeDetails = new java.util.HashMap<>();
            changeDetails.put("old", oldValues);
            changeDetails.put("new", newValues);

            // 序列化变更详情
            String changeData = objectMapper.writeValueAsString(changeDetails);
            record.setBeforeData(objectMapper.writeValueAsString(oldValues));
            record.setAfterData(objectMapper.writeValueAsString(newValues));
            
            return updateRecordRepository.save(record);
        } catch (Exception e) {
            // 记录日志但不中断主流程
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 记录实体删除操作
     */
    @Override
    public UpdateRecord logDelete(BaseEntity entity, String operator, String description) {
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType(entity.getClass().getSimpleName());
            record.setEntityId(entity.getId());
            record.setEntityName(entity.get__name__());
            record.setOperationType(UpdateRecord.OperationType.DELETE);
            record.setOperator(operator);
            record.setDescription(description);
            
            // 序列化旧实体作为beforeData
            String beforeData = objectMapper.writeValueAsString(entity);
            record.setBeforeData(beforeData);
            
            return updateRecordRepository.save(record);
        } catch (Exception e) {
            // 记录日志但不中断主流程
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取所有更新记录
     */
    @Override
    public List<UpdateRecord> findAllUpdateRecords() {
        return updateRecordRepository.findAll();
    }
    
    /**
     * 根据ID获取更新记录
     */
    @Override
    public UpdateRecord findUpdateRecordById(Long id) {
        return updateRecordRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据ID删除更新记录
     */
    @Override
    public void deleteUpdateRecordById(Long id) {
        updateRecordRepository.deleteById(id);
    }
    
    /**
     * 根据实体类型获取更新记录
     */
    @Override
    public List<UpdateRecord> findUpdateRecordsByEntityType(String entityType) {
        return updateRecordRepository.findByEntityType(entityType);
    }
    
    /**
     * 根据操作类型获取更新记录
     */
    @Override
    public List<UpdateRecord> findUpdateRecordsByOperationType(UpdateRecord.OperationType operationType) {
        return updateRecordRepository.findByOperationType(operationType);
    }
    
    /**
     * 根据操作用户获取更新记录
     */
    @Override
    public List<UpdateRecord> findUpdateRecordsByOperator(String operator) {
        return updateRecordRepository.findByOperator(operator);
    }
    
    /**
     * 根据实体ID获取更新记录
     */
    @Override
    public List<UpdateRecord> findUpdateRecordsByEntityId(Long entityId) {
        return updateRecordRepository.findByEntityId(entityId);
    }
}