// src/main/java/com/boylegu/springboot_vue/service/impl/ProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.service.ProcessDiagramService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ProcessDiagramServiceImpl<T extends ProcessDiagram, R extends org.springframework.data.jpa.repository.JpaRepository<T, Long>>
        implements ProcessDiagramService<T> {

    protected R repository;

    public ProcessDiagramServiceImpl(R repository) {
        this.repository = repository;
    }

    @Override
    public List<T> getAllDiagrams() {
        return repository.findAll();
    }

    @Override
    public T getDiagramById(Long id) {
        Optional<T> diagram = repository.findById(id);
        return diagram.orElse(null);
    }

    @Override
    public T createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        T diagram = createNewInstance();
        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
    public T updateDiagram(Long id, String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        T diagram = getDiagramById(id);
        if (diagram == null) {
            throw new RuntimeException("Diagram not found with id: " + id);
        }

        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    // 这是一个额外的受保护方法，用于保存图表和处理文件上传
    protected T saveDiagram(T diagram, MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                diagram.setImageData(imageFile.getBytes());

                // 使用 ImageType 设置 image type
                String originalFilename = imageFile.getOriginalFilename();
                if (originalFilename != null && originalFilename.contains(".")) {
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    ProcessDiagram.ImageType imageType = ProcessDiagram.ImageType.fromExtension(extension);
                    if (imageType != null) {
                        diagram.setImageType(imageType);
                    }
                }
            }
            return repository.save(diagram);
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败", e);
        }
    }

    @Override
    public void deleteDiagram(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<T> getDiagramsByIsValid(Boolean isValid) {
        // 使用反射调用findByIsValid方法，因为JpaRepository接口中没有这个方法
        try {
            return (List<T>) repository.getClass().getMethod("findByIsValid", Boolean.class).invoke(repository, isValid);
        } catch (Exception e) {
            throw new RuntimeException("调用findByIsValid方法失败", e);
        }
    }

    @Override
    public T activateDiagram(Long id) {
        Optional<T> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            T diagram = diagramOptional.get();
            diagram.setValid(true);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public T deactivateDiagram(Long id) {
        Optional<T> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            T diagram = diagramOptional.get();
            diagram.setValid(false);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public Map<Long, T> getProcessDiagramsMapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        List<T> diagrams = repository.findAllById(ids);
        return diagrams.stream()
                .collect(Collectors.toMap(ProcessDiagram::getId, diagram -> diagram));
    }
}