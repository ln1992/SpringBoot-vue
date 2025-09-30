// src/main/java/com/boylegu/springboot_vue/service/impl/UpdateRecordServiceImpl.java
package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.entities.UpdateRecord;
import com.douyu.springboot_vue.entities.BaseEntity;
import com.douyu.springboot_vue.repository.UpdateRecordRepository;
import com.douyu.springboot_vue.service.UpdateRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 更新记录服务实现类
 */
@Service
public class UpdateRecordServiceImpl implements UpdateRecordService {

    @Autowired
    private UpdateRecordRepository updateRecordRepository;
    @Override
    public UpdateRecord logCreate(String entityType, Long entityId, String entityName,
                                  String operator, String description, String afterData) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setEntityName(entityName);
        record.setOperator(operator);
        record.setOperationType(UpdateRecord.OperationType.CREATE);
        record.setDescription(description);
        record.setAfterData(afterData);

        return updateRecordRepository.save(record);
    }

    @Override
    public UpdateRecord logUpdate(String entityType, Long entityId, String entityName,
                                  String beforeData, String afterData,
                                  String operator, String description) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setEntityName(entityName);
        record.setOperator(operator);
        record.setOperationType(UpdateRecord.OperationType.UPDATE);
        record.setDescription(description);
        record.setBeforeData(beforeData);
        record.setAfterData(afterData);

        return updateRecordRepository.save(record);
    }

    @Override
    public UpdateRecord logDelete(String entityType, Long entityId, String entityName,
                                  String operator, String description, String beforeData) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setEntityName(entityName);
        record.setOperator(operator);
        record.setOperationType(UpdateRecord.OperationType.DELETE);
        record.setDescription(description);
        record.setBeforeData(beforeData);

        return updateRecordRepository.save(record);
    }
    @Override
    public UpdateRecord logBatchCopy(String operator, String description, Long version) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType("Batch");
        record.setEntityId(version);
        record.setEntityName("版本 " + version);
        record.setOperator(operator);
        record.setOperationType(UpdateRecord.OperationType.BATCH_COPY);
        record.setDescription(description);

        return updateRecordRepository.save(record);
    }

    @Override
    public UpdateRecord logBatchPublish(String operator, String description, Long version) {
        UpdateRecord record = new UpdateRecord();
        record.setEntityType("Batch");
        record.setEntityId(version);
        record.setEntityName("版本 " + version);
        record.setOperator(operator);
        record.setOperationType(UpdateRecord.OperationType.BATCH_PUBLISH);
        record.setDescription(description);

        return updateRecordRepository.save(record);
    }

    @Override
    public List<UpdateRecord> findAllUpdateRecords() {
        return updateRecordRepository.findAll();
    }

    @Override
    public UpdateRecord findUpdateRecordById(Long id) {
        return updateRecordRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteUpdateRecordById(Long id) {
        updateRecordRepository.deleteById(id);
    }

    @Override
    public List<UpdateRecord> findUpdateRecordsByEntityType(String entityType) {
        return updateRecordRepository.findByEntityType(entityType);
    }

    @Override
    public List<UpdateRecord> findUpdateRecordsByOperationType(UpdateRecord.OperationType operationType) {
        return updateRecordRepository.findByOperationType(operationType);
    }

    @Override
    public List<UpdateRecord> findUpdateRecordsByOperator(String operator) {
        return updateRecordRepository.findByOperator(operator);
    }

    @Override
    public List<UpdateRecord> findUpdateRecordsByEntityId(Long entityId) {
        return updateRecordRepository.findByEntityId(entityId);
    }

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
