// src/main/java/com/boylegu/springboot_vue/aop/annotation/RecordUpdate.java
package com.boylegu.springboot_vue.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记需要记录更新操作的方法注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordUpdate {
    /**
     * 操作类型
     */
    OperationType operation() default OperationType.UPDATE;
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 实体类型（可选，如果不指定则自动从方法参数或返回值推断）
     */
    String entityType() default "";
    
    /**
     * 操作类型枚举
     */
    enum OperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除");
        
        private final String description;
        
        OperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
