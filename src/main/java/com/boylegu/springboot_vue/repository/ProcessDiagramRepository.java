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
    // 这是一个通用的Repository接口，具体的Repository应该继承它
}
