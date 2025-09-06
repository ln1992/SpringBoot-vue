// src/main/java/com/boylegu/springboot_vue/controller/BaseProcessDiagramController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.service.ProcessDiagramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

public abstract class ProcessDiagramController<T extends ProcessDiagram> {

    protected static final Logger logger = Logger.getLogger(ProcessDiagramController.class.getName());

    protected ProcessDiagramService<T> service;
    protected String entityName;

    public ProcessDiagramController(ProcessDiagramService<T> service, String entityName) {
        this.service = service;
        this.entityName = entityName;
    }

    // 获取所有流程图
    public ResponseEntity<List<T>> getAllDiagrams() {
        try {
            List<T> diagrams = service.getAllDiagrams();
            logger.info("Successfully retrieved " + diagrams.size() + " " + entityName + " diagrams");
            return ResponseEntity.ok(diagrams);
        } catch (Exception e) {
            logger.severe("Error retrieving " + entityName + " diagrams: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID获取流程图
    public ResponseEntity<T> getDiagramById(Long id) {
        try {
            T diagram = service.getDiagramById(id);
            if (diagram != null) {
                return ResponseEntity.ok(diagram);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error retrieving " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 创建新的流程图
    public ResponseEntity<?> createDiagram(
            String imageName,
            Long version,
            MultipartFile imageFile,
            Boolean isValid) {
        try {
            logger.info("Creating " + entityName + " diagram with name: " + imageName);

            T diagram = createNewInstance();
            diagram.setImageName(imageName);
            if (version != null) {
                diagram.setVersion(version);
            }
            diagram.setValid(isValid);

            T savedDiagram = service.saveDiagram(diagram, imageFile);
            logger.info(entityName + " diagram created successfully with ID: " + savedDiagram.getId());
            return ResponseEntity.ok(savedDiagram);
        } catch (Exception e) {
            logger.severe("Error creating " + entityName + " diagram: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("创建" + entityName + "失败: " + e.getMessage());
        }
    }

    // 更新流程图
    public ResponseEntity<?> updateDiagram(
            Long id,
            String imageName,
            Long version,
            MultipartFile imageFile,
            Boolean isValid) {
        try {
            logger.info("Updating " + entityName + " diagram ID " + id + " with name: " + imageName);

            T diagram = service.getDiagramById(id);
            if (diagram != null) {
                // 更新字段
                diagram.setImageName(imageName);
                if (version != null) {
                    diagram.setVersion(version);
                }
                diagram.setValid(isValid);

                T updatedDiagram = service.saveDiagram(diagram, imageFile);
                logger.info(entityName + " diagram updated successfully with ID: " + updatedDiagram.getId());
                return ResponseEntity.ok(updatedDiagram);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error updating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("更新" + entityName + "失败: " + e.getMessage());
        }
    }

    // 删除流程图
    public ResponseEntity<Void> deleteDiagram(Long id) {
        try {
            T diagram = service.getDiagramById(id);
            if (diagram != null) {
                service.deleteDiagram(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 启用流程图
    public ResponseEntity<?> activateDiagram(Long id) {
        try {
            boolean success = service.activateDiagram(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error activating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("启用" + entityName + "失败: " + e.getMessage());
        }
    }

    // 禁用流程图
    public ResponseEntity<?> deactivateDiagram(Long id) {
        try {
            boolean success = service.deactivateDiagram(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deactivating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("禁用" + entityName + "失败: " + e.getMessage());
        }
    }

    // 根据状态获取流程图
    public ResponseEntity<List<T>> getDiagramsByValidStatus(Boolean isValid) {
        try {
            List<T> diagrams = service.getDiagramsByIsValid(isValid);
            return ResponseEntity.ok(diagrams);
        } catch (Exception e) {
            logger.severe("Error retrieving " + entityName + " diagrams by valid status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 抽象方法，子类需要实现以创建具体实例
    protected abstract T createNewInstance();
}
