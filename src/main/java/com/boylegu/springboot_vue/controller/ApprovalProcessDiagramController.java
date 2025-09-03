// src/main/java/com/boylegu/springboot_vue/controller/ApprovalProcessDiagramController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.service.ApprovalProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/process-diagrams/approval")
@CrossOrigin(origins = "*") // 允许跨域访问
public class ApprovalProcessDiagramController extends ProcessDiagramController<ApprovalProcessDiagram> {

    @Autowired
    public ApprovalProcessDiagramController(ApprovalProcessDiagramService approvalProcessDiagramService) {
        super(approvalProcessDiagramService, "审批流程图");
    }

    // 获取所有审批流程图
    @GetMapping
    public ResponseEntity<List<ApprovalProcessDiagram>> getAllDiagrams() {
        return super.getAllDiagrams();
    }

    // 根据ID获取审批流程图
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalProcessDiagram> getDiagramById(@PathVariable Long id) {
        return super.getDiagramById(id);
    }

    // 创建新的审批流程图
    @PostMapping
    public ResponseEntity<?> createDiagram(
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", required = false) Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.createDiagram(imageName, version, imageFile, isValid);
    }

    // 更新审批流程图
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiagram(
            @PathVariable Long id,
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "version", required = false) Long version,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.updateDiagram(id, imageName, version, imageFile, isValid);
    }

    // 删除审批流程图
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagram(@PathVariable Long id) {
        return super.deleteDiagram(id);
    }

    // 启用审批流程图
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateDiagram(@PathVariable Long id) {
        return super.activateDiagram(id);
    }

    // 禁用审批流程图
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateDiagram(@PathVariable Long id) {
        return super.deactivateDiagram(id);
    }

    // 根据状态获取审批流程图
    @GetMapping("/search/valid")
    public ResponseEntity<List<ApprovalProcessDiagram>> getDiagramsByValidStatus(@RequestParam Boolean isValid) {
        return super.getDiagramsByValidStatus(isValid);
    }

    @Override
    protected ApprovalProcessDiagram createNewInstance() {
        return new ApprovalProcessDiagram();
    }
}
