// src/main/java/com/boylegu/springboot_vue/repository/BusinessProcessDiagramRepository.java
package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.BusinessProcessDiagram;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusinessProcessDiagramRepository extends ProcessDiagramRepository<BusinessProcessDiagram> {
    // 在具体实体的Repository中添加方法
    List<BusinessProcessDiagram> findByIsValid(Boolean isValid);
}