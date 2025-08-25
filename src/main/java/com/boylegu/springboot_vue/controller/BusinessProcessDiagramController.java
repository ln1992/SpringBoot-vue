// src/main/java/com/boylegu/springboot_vue/controller/BusinessProcessDiagramController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/process-diagrams/business")
@CrossOrigin(origins = "*") // 允许跨域访问
public class BusinessProcessDiagramController extends ProcessDiagramController<BusinessProcessDiagram> {

    @Autowired
    public BusinessProcessDiagramController(BusinessProcessDiagramService businessProcessDiagramService) {
        super(businessProcessDiagramService, "业务流程图");
    }

    // 获取所有业务流程图
    @GetMapping
    public ResponseEntity<List<BusinessProcessDiagram>> getAllDiagrams() {
        return super.getAllDiagrams();
    }

    // 根据ID获取业务流程图
    @GetMapping("/{id}")
    public ResponseEntity<BusinessProcessDiagram> getDiagramById(@PathVariable Long id) {
        return super.getDiagramById(id);
    }

    // 创建新的业务流程图
    @PostMapping
    public ResponseEntity<?> createDiagram(
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.createDiagram(imageName, imageFile, isValid);
    }

    // 更新业务流程图
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiagram(
            @PathVariable Long id,
            @RequestParam("imageName") String imageName,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isValid", defaultValue = "true") Boolean isValid) {
        return super.updateDiagram(id, imageName, imageFile, isValid);
    }

    // 删除业务流程图
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagram(@PathVariable Long id) {
        return super.deleteDiagram(id);
    }

    // 启用业务流程图
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateDiagram(@PathVariable Long id) {
        return super.activateDiagram(id);
    }

    // 禁用业务流程图
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateDiagram(@PathVariable Long id) {
        return super.deactivateDiagram(id);
    }

    // 根据状态获取业务流程图
    @GetMapping("/search/valid")
    public ResponseEntity<List<BusinessProcessDiagram>> getDiagramsByValidStatus(@RequestParam Boolean isValid) {
        return super.getDiagramsByValidStatus(isValid);
    }

    @Override
    protected BusinessProcessDiagram createNewInstance() {
        return new BusinessProcessDiagram();
    }
}
