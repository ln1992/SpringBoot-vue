// src/main/java/com/boylegu/springboot_vue/service/ProcessDiagramService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProcessDiagramService<T extends ProcessDiagram> {
    List<T> getAllDiagrams();

    T getDiagramById(Long id);

    T saveDiagram(T diagram, MultipartFile imageFile);

    void deleteDiagram(Long id);

    boolean activateDiagram(Long id);

    boolean deactivateDiagram(Long id);

    // 添加这个方法
    List<T> getDiagramsByIsValid(Boolean isValid);
}
