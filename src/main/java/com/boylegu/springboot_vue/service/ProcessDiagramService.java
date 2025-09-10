// src/main/java/com/boylegu/springboot_vue/service/ProcessDiagramService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProcessDiagramService<T extends ProcessDiagram> {
    List<T> getAllDiagrams();

    T getDiagramById(Long id);

    T createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid);

    T updateDiagram(Long id, String imageName, Long version, MultipartFile imageFile, Boolean isValid);

    void deleteDiagram(Long id);

    T activateDiagram(Long id);

    T deactivateDiagram(Long id);

    List<T> getDiagramsByIsValid(Boolean isValid);

    // 工厂方法，用于创建新实例
    T createNewInstance();

    Map<Long, T> getProcessDiagramsMapByIds(List<Long> ids);

}
