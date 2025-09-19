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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    @Override
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
     * 记录批量拷贝操作
     */
    @Override
    public UpdateRecord logBatchCopy(String operator, String description, Long version) {
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType("Matter");
            record.setEntityId(-1L);
            record.setEntityName("批量拷贝操作");
            record.setOperationType(UpdateRecord.OperationType.BATCH_COPY);
            record.setOperator(operator);
            record.setDescription(description + "，版本号：" + version);
            
            // 记录版本信息
            java.util.Map<String, Object> versionInfo = new java.util.HashMap<>();
            versionInfo.put("version", version);
            String afterData = objectMapper.writeValueAsString(versionInfo);
            record.setAfterData(afterData);
            
            return updateRecordRepository.save(record);
        } catch (Exception e) {
            // 记录日志但不中断主流程
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 记录批量发布操作
     */
    @Override
    public UpdateRecord logBatchPublish(String operator, String description, Long version) {
        try {
            UpdateRecord record = new UpdateRecord();
            record.setEntityType("Matter");
            record.setEntityId(-1L);
            record.setEntityName("批量发布操作");
            record.setOperationType(UpdateRecord.OperationType.BATCH_PUBLISH);
            record.setOperator(operator);
            record.setDescription(description + "，版本号：" + version);
            
            // 记录版本信息
            java.util.Map<String, Object> versionInfo = new java.util.HashMap<>();
            versionInfo.put("version", version);
            String afterData = objectMapper.writeValueAsString(versionInfo);
            record.setAfterData(afterData);
            
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
    
    /**
     * 根据过滤条件和排序条件获取更新记录
     */
    @Override
    public List<UpdateRecord> findUpdateRecordsWithFilters(String entityName, String entityType, 
                                                         String operationType, String sortBy, String sortDirection) {
        List<UpdateRecord> records = findAllUpdateRecords();
        
        // 应用过滤条件
        if (entityName != null && !entityName.isEmpty()) {
            records = records.stream()
                    .filter(record -> record.getEntityName() != null && 
                            record.getEntityName().toLowerCase().contains(entityName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (entityType != null && !entityType.isEmpty()) {
            records = records.stream()
                    .filter(record -> record.getEntityType() != null && 
                            record.getEntityType().toLowerCase().contains(entityType.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (operationType != null && !operationType.isEmpty()) {
            records = records.stream()
                    .filter(record -> record.getOperationType() != null && 
                            record.getOperationType().name().toLowerCase().contains(operationType.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // 应用排序
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy.toLowerCase()) {
                case "createdtime":
                    records.sort(Comparator.comparing(UpdateRecord::getCreatedTime));
                    break;
                case "entityname":
                    records.sort(Comparator.comparing(UpdateRecord::getEntityName, 
                            Comparator.nullsFirst(Comparator.naturalOrder())));
                    break;
                case "entitytype":
                    records.sort(Comparator.comparing(UpdateRecord::getEntityType, 
                            Comparator.nullsFirst(Comparator.naturalOrder())));
                    break;
                case "operationtype":
                    records.sort(Comparator.comparing(UpdateRecord::getOperationType, 
                            Comparator.nullsFirst(Comparator.naturalOrder())));
                    break;
                default:
                    // 默认按创建时间排序
                    records.sort(Comparator.comparing(UpdateRecord::getCreatedTime));
                    break;
            }
            
            // 如果是降序，则反转排序结果
            if ("desc".equalsIgnoreCase(sortDirection)) {
                java.util.Collections.reverse(records);
            }
        } else {
            // 默认按创建时间降序排序
            records.sort(Comparator.comparing(UpdateRecord::getCreatedTime).reversed());
        }
        
        return records;
    }
}