// src/main/java/com/boylegu/springboot_vue/controller/ApprovalProcessDiagramController.java
package com.douyu.springboot_vue.controller;

import com.douyu.springboot_vue.entities.ApprovalProcessDiagram;
import com.douyu.springboot_vue.service.ApprovalProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/process-diagrams/approval")
@CrossOrigin(origins = "*")
public class ApprovalProcessDiagramController extends ProcessDiagramController<ApprovalProcessDiagram> {

    protected static final Logger logger = Logger.getLogger(ApprovalProcessDiagramController.class.getName());

    private final ApprovalProcessDiagramService approvalProcessDiagramService;

    @Autowired
    public ApprovalProcessDiagramController(ApprovalProcessDiagramService approvalProcessDiagramService) {
        super(approvalProcessDiagramService, "Approval");
        this.approvalProcessDiagramService = approvalProcessDiagramService;
    }

    // 获取所有审批流程图（分页等）
    @GetMapping
    public ResponseEntity<?> getApprovalProcessDiagrams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Fetching approval process diagrams with page: " + page + ", size: " + size);
            // 这里可以添加分页逻辑
            List<ApprovalProcessDiagram> diagrams = approvalProcessDiagramService.getAllDiagrams();
            logger.info("Successfully retrieved " + diagrams.size() + " approval process diagrams");
            return ResponseEntity.ok(diagrams);
        } catch (Exception e) {
            logger.severe("Error retrieving approval process diagrams: " + e.getMessage());
            return ResponseEntity.status(500).body("获取审批流程图失败: " + e.getMessage());
        }
    }

    // 根据ID获取审批流程图
    public ResponseEntity<ApprovalProcessDiagram> getApprovalProcessDiagramById(@PathVariable Long id) {
        return super.getDiagramById(id);
    }

    // 创建审批流程图
    @PostMapping
    public ResponseEntity<?> createApprovalProcessDiagram(
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", defaultValue = "1") Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.createDiagram(imageName, version, imageFile, isValid);
    }

    // 更新审批流程图
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApprovalProcessDiagram(
            @PathVariable Long id,
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", required = false) Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", required = false) Boolean isValid) {
        return super.updateDiagram(id, imageName, version, imageFile, isValid);
    }

    // 删除审批流程图
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprovalProcessDiagram(@PathVariable Long id) {
        return super.deleteDiagram(id);
    }

    // 启用审批流程图
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateApprovalProcessDiagram(@PathVariable Long id) {
        return super.activateDiagram(id);
    }

    // 停用审批流程图
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateApprovalProcessDiagram(@PathVariable Long id) {
        return super.deactivateDiagram(id);
    }
}