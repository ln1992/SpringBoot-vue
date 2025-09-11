// src/main/java/com/boylegu/springboot_vue/repository/UpdateRecordRepository.java
package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateRecordRepository extends JpaRepository<UpdateRecord, Long> {
    
    /**
     * 根据实体类型和ID查找更新记录
     */
    List<UpdateRecord> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * 根据操作类型查找更新记录
     */
    List<UpdateRecord> findByOperationType(UpdateRecord.OperationType operationType);
    
    /**
     * 根据操作用户查找更新记录
     */
    List<UpdateRecord> findByOperator(String operator);
    
    /**
     * 根据实体类型查找更新记录
     */
    List<UpdateRecord> findByEntityType(String entityType);
}
