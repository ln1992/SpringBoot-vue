// src/main/java/com/boylegu/springboot_vue/repository/ProcessDiagramRepository.java
package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ProcessDiagram实体的Repository接口基类
 */
@Repository
public interface ProcessDiagramRepository<T extends ProcessDiagram> extends JpaRepository<T, Long> {
    // 移除 findByIsValid 方法，因为 ProcessDiagram 不是实体类
}
