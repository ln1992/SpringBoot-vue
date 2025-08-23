// src/main/java/com/boylegu/springboot_vue/service/BaseProcessDiagramService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProcessDiagramService<T extends ProcessDiagram> {
    List<T> getAllDiagrams();
    T getDiagramById(Long id);
    T saveDiagram(T diagram);
    T saveDiagramWithImage(MultipartFile imageFile, String imageName) throws IOException;
    T updateDiagram(Long id, MultipartFile imageFile, String imageName) throws IOException;
    void deleteDiagram(Long id);
}
