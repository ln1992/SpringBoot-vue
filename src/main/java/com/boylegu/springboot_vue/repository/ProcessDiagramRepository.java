// src/main/java/com/boylegu/springboot_vue/repository/ProcessDiagramRepository.java
package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProcessDiagram实体的Repository接口基类
 */
@Repository
public interface ProcessDiagramRepository<T extends ProcessDiagram> extends JpaRepository<T, Long> {
    // 移除 findByIsValid 方法，因为 ProcessDiagram 不是实体类

    /**
     * 查询所有唯一的版本号
     * @return 版本号列表
     */
    @Query("SELECT DISTINCT pd.version FROM #{#entityName} pd WHERE pd.version IS NOT NULL ORDER BY pd.version")
    List<Long> findAllVersions();
}
