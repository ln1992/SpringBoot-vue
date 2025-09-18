// src/main/java/com/boylegu/springboot_vue/controller/ProcessDiagramController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.service.ProcessDiagramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    // 获取所有流程图（用于拷贝功能）
    @GetMapping("/all")
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
    @GetMapping("/{id}")
    public ResponseEntity<T> getDiagramById(@PathVariable Long id) {
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
    
    // 获取所有版本号
    @GetMapping("/versions")
    public ResponseEntity<List<Long>> findAllDiagramVersions() {
        try {
            List<Long> versions = service.findAllDiagramVersions();
            logger.info("Successfully retrieved " + versions.size() + " " + entityName + " versions");
            return ResponseEntity.ok(versions);
        } catch (Exception e) {
            logger.severe("Error retrieving " + entityName + " versions: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 创建新的流程图
    public ResponseEntity<?> createDiagram(
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", defaultValue = "1") Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        try {
            logger.info("Creating " + entityName + " diagram with name: " + imageName);
            T savedDiagram = service.createDiagram(imageName, version, imageFile, isValid);
            logger.info(entityName + " diagram created successfully with ID: " + savedDiagram.getId());
            return ResponseEntity.ok(savedDiagram);
        } catch (Exception e) {
            logger.severe("Error creating " + entityName + " diagram: " + e.getMessage());
            return ResponseEntity.status(500).body("创建" + entityName + "失败: " + e.getMessage());
        }
    }

    // 更新流程图
    public ResponseEntity<?> updateDiagram(
            @PathVariable Long id,
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", required = false) Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", required = false) Boolean isValid) {
        try {
            logger.info("Updating " + entityName + " diagram ID " + id + " with name: " + imageName);
            T updatedDiagram = service.updateDiagram(id, imageName, version, imageFile, isValid);
            logger.info(entityName + " diagram updated successfully with ID: " + updatedDiagram.getId());
            return ResponseEntity.ok(updatedDiagram);
        } catch (Exception e) {
            logger.severe("Error updating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("更新" + entityName + "失败: " + e.getMessage());
        }
    }

    // 删除流程图
    public ResponseEntity<Void> deleteDiagram(@PathVariable Long id) {
        try {
            service.deleteDiagram(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("Error deleting " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 启用流程图
    public ResponseEntity<?> activateDiagram(@PathVariable Long id) {
        try {
            T diagram = service.activateDiagram(id);
            if (diagram != null) {
                return ResponseEntity.ok(diagram);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error activating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("启用" + entityName + "失败: " + e.getMessage());
        }
    }

    // 禁用流程图
    public ResponseEntity<?> deactivateDiagram(@PathVariable Long id) {
        try {
            T diagram = service.deactivateDiagram(id);
            if (diagram != null) {
                return ResponseEntity.ok(diagram);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deactivating " + entityName + " diagram with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("禁用" + entityName + "失败: " + e.getMessage());
        }
    }

    // 根据状态获取流程图
    public ResponseEntity<List<T>> getDiagramsByValidStatus(@RequestParam Boolean isValid) {
        try {
            List<T> diagrams = service.getDiagramsByIsValid(isValid);
            return ResponseEntity.ok(diagrams);
        } catch (Exception e) {
            logger.severe("Error retrieving " + entityName + " diagrams by valid status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}