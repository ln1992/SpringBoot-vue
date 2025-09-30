// src/main/java/com/boylegu/springboot_vue/service/UpdateRecordService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import com.boylegu.springboot_vue.entities.BaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 更新记录服务接口
 */
public interface UpdateRecordService {

    /**
     * 记录实体创建操作
     */
    UpdateRecord logCreate(String entityType, Long entityId, String entityName,
                           String operator, String description, String afterData);

    /**
     * 记录实体更新操作
     */
    UpdateRecord logUpdate(String entityType, Long entityId, String entityName,
                           String beforeData, String afterData,
                           String operator, String description);

    /**
     * 记录实体删除操作
     */
    UpdateRecord logDelete(String entityType, Long entityId, String entityName,
                           String operator, String description, String beforeData);

    /**
     * 记录批量拷贝操作
     */
    UpdateRecord logBatchCopy(String operator, String description, Long version);

    /**
     * 记录批量发布操作
     */
    UpdateRecord logBatchPublish(String operator, String description, Long version);

    /**
     * 获取所有更新记录
     */
    List<UpdateRecord> findAllUpdateRecords();

    /**
     * 根据ID获取更新记录
     */
    UpdateRecord findUpdateRecordById(Long id);

    /**
     * 根据ID删除更新记录
     */
    void deleteUpdateRecordById(Long id);

    /**
     * 根据实体类型获取更新记录
     */
    List<UpdateRecord> findUpdateRecordsByEntityType(String entityType);

    /**
     * 根据操作类型获取更新记录
     */
    List<UpdateRecord> findUpdateRecordsByOperationType(UpdateRecord.OperationType operationType);

    /**
     * 根据操作用户获取更新记录
     */
    List<UpdateRecord> findUpdateRecordsByOperator(String operator);

    /**
     * 根据实体ID获取更新记录
     */
    List<UpdateRecord> findUpdateRecordsByEntityId(Long entityId);

    /**
     * 根据过滤条件和排序条件获取更新记录
     */
    List<UpdateRecord> findUpdateRecordsWithFilters(String entityName, String entityType,
                                                    String operationType, String sortBy, String sortDirection);
}
