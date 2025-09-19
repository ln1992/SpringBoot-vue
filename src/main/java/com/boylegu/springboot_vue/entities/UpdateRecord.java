// src/main/java/com/boylegu/springboot_vue/entities/UpdateRecord.java
package com.boylegu.springboot_vue.entities;

import javax.persistence.*;

/**
 * 更新记录实体类，用于记录每个实体的变更情况
 */
@Entity
@Table(name = "update_record")
public class UpdateRecord extends BaseEntity {
    
    // 实体类型（如 Material, ProcessDiagram 等）
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    // 实体ID
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    // 实体名称
    @Column(name = "entity_name")
    private String entityName;
    
    // 操作类型（CREATE, UPDATE, DELETE）
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;
    
    // 变更前的数据（JSON格式）
    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;
    
    // 变更后的数据（JSON格式）
    @Column(name = "after_data", columnDefinition = "TEXT")
    private String afterData;
    
    // 操作用户
    @Column(name = "operator")
    private String operator;
    
    // 操作描述
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 默认构造函数
    public UpdateRecord() {}
    
    // 带参数的构造函数
    public UpdateRecord(String entityType, Long entityId, String entityName, 
                       OperationType operationType, String beforeData, 
                       String afterData, String operator, String description) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.operationType = operationType;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.operator = operator;
        this.description = description;
    }
    
    // 枚举类型：操作类型
    public enum OperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除"),
        BATCH_COPY("批量拷贝"),
        BATCH_PUBLISH("批量发布");
        
        private final String description;
        
        OperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    // Getter和Setter方法
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public String getBeforeData() {
        return beforeData;
    }
    
    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }
    
    public String getAfterData() {
        return afterData;
    }
    
    public void setAfterData(String afterData) {
        this.afterData = afterData;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "UpdateRecord{" +
                super.toString() +  // 包含父类的所有字段
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", entityName='" + entityName + '\'' +
                ", operationType=" + operationType +
                ", beforeData='" + beforeData + '\'' +
                ", afterData='" + afterData + '\'' +
                ", operator='" + operator + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
