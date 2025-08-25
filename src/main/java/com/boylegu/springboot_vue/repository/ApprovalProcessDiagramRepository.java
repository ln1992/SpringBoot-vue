// src/main/java/com/boylegu/springboot_vue/repository/ApprovalProcessDiagramRepository.java
package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalProcessDiagramRepository extends ProcessDiagramRepository<ApprovalProcessDiagram> {
    // 在具体实体的Repository中添加方法
    List<ApprovalProcessDiagram> findByIsValid(Boolean isValid);
}
