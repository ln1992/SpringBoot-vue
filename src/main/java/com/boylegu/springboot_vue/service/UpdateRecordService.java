// src/main/java/com/boylegu/springboot_vue/service/UpdateRecordService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import com.boylegu.springboot_vue.entities.BaseEntity;

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
}
