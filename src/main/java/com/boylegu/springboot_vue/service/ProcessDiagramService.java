// src/main/java/com/boylegu/springboot_vue/service/ProcessDiagramService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProcessDiagramService<T extends ProcessDiagram> {
    List<T> getAllDiagrams();

    T getDiagramById(Long id);

    T createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid);

    T updateDiagram(Long id, String imageName, Long version, MultipartFile imageFile, Boolean isValid);

    void deleteDiagram(Long id);

    boolean activateDiagram(Long id);

    boolean deactivateDiagram(Long id);

    List<T> getDiagramsByIsValid(Boolean isValid);

    // 工厂方法，用于创建新实例
    T createNewInstance();
}
