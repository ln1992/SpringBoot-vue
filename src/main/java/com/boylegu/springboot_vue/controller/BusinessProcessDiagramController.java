// src/main/java/com/boylegu/springboot_vue/controller/BusinessProcessDiagramController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/process-diagrams/business")
@CrossOrigin(origins = "*")
public class BusinessProcessDiagramController extends ProcessDiagramController<BusinessProcessDiagram> {

    protected static final Logger logger = Logger.getLogger(BusinessProcessDiagramController.class.getName());

    private final BusinessProcessDiagramService businessProcessDiagramService;

    @Autowired
    public BusinessProcessDiagramController(BusinessProcessDiagramService businessProcessDiagramService) {
        super(businessProcessDiagramService, "Business");
        this.businessProcessDiagramService = businessProcessDiagramService;
    }

    // 获取所有业务流程图
    @GetMapping
    public ResponseEntity<?> getBusinessProcessDiagrams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Fetching business process diagrams with page: " + page + ", size: " + size);
            // 这里可以添加分页逻辑
            List<BusinessProcessDiagram> diagrams = businessProcessDiagramService.getAllDiagrams();
            logger.info("Successfully retrieved " + diagrams.size() + " business process diagrams");
            return ResponseEntity.ok(diagrams);
        } catch (Exception e) {
            logger.severe("Error retrieving business process diagrams: " + e.getMessage());
            return ResponseEntity.status(500).body("获取业务流程图失败: " + e.getMessage());
        }
    }

    // 根据ID获取业务流程图
    public ResponseEntity<BusinessProcessDiagram> getBusinessProcessDiagramById(@PathVariable Long id) {
        return super.getDiagramById(id);
    }

    // 创建业务流程图
    @PostMapping
    public ResponseEntity<?> createBusinessProcessDiagram(
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", defaultValue = "1") Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.createDiagram(imageName, version, imageFile, isValid);
    }

    // 更新业务流程图
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBusinessProcessDiagram(
            @PathVariable Long id,
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", required = false) Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", required = false) Boolean isValid) {
        return super.updateDiagram(id, imageName, version, imageFile, isValid);
    }

    // 删除业务流程图
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessProcessDiagram(@PathVariable Long id) {
        return super.deleteDiagram(id);
    }

    // 启用业务流程图
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateBusinessProcessDiagram(@PathVariable Long id) {
        return super.activateDiagram(id);
    }

    // 停用业务流程图
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateBusinessProcessDiagram(@PathVariable Long id) {
        return super.deactivateDiagram(id);
    }
}