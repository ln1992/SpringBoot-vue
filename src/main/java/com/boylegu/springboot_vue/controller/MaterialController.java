package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*") // 允许跨域访问
public class MaterialController {
    
    @Autowired
    private MaterialService materialService;
    
    // 获取所有材料
    @GetMapping
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterials();
    }
    
    // 根据ID获取材料
    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        if (material != null) {
            return ResponseEntity.ok(material);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 创建新材料
    @PostMapping
    public Material createMaterial(@RequestBody Material material) {
        return materialService.saveMaterial(material);
    }
    
    // 更新材料
    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @RequestBody Material materialDetails) {
        Material material = materialService.getMaterialById(id);
        if (material != null) {
            material.setMaterialDetails(materialDetails.getMaterialDetails());
            material.setReviewPoints(materialDetails.getReviewPoints());
            material.setAutoApprovalCriteria(materialDetails.getAutoApprovalCriteria());
            material.setShared(materialDetails.getShared());
            material.setMaterialSource(materialDetails.getMaterialSource());
            material.setProcessingMethodAndInfoAccess(materialDetails.getProcessingMethodAndInfoAccess());
            material.setEligibleForPromise(materialDetails.getEligibleForPromise());
            
            Material updatedMaterial = materialService.saveMaterial(material);
            return ResponseEntity.ok(updatedMaterial);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 删除材料
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        if (material != null) {
            materialService.deleteMaterial(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
