// src/main/java/com/boylegu/springboot_vue/service/UpdateRecordService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import com.boylegu.springboot_vue.entities.BaseEntity;

import java.util.List;

/**
 * 更新记录服务接口
 */
public interface UpdateRecordService {

    /**
     * 记录实体创建操作
     */
    UpdateRecord logCreate(BaseEntity entity, String operator, String description);

    /**
     * 记录实体更新操作
     */
    UpdateRecord logUpdate(BaseEntity oldEntity, BaseEntity newEntity, String operator, String description);

    /**
     * 记录实体删除操作
     */
    UpdateRecord logDelete(BaseEntity entity, String operator, String description);
    
    /**
     * 获取所有更新记录
     */
    List<UpdateRecord> getAllUpdateRecords();
    
    /**
     * 根据ID获取更新记录
     */
    UpdateRecord getUpdateRecordById(Long id);
    
    /**
     * 根据ID删除更新记录
     */
    void deleteUpdateRecordById(Long id);
    
    /**
     * 根据实体类型获取更新记录
     */
    List<UpdateRecord> getUpdateRecordsByEntityType(String entityType);
    
    /**
     * 根据操作类型获取更新记录
     */
    List<UpdateRecord> getUpdateRecordsByOperationType(UpdateRecord.OperationType operationType);
    
    /**
     * 根据操作用户获取更新记录
     */
    List<UpdateRecord> getUpdateRecordsByOperator(String operator);
}