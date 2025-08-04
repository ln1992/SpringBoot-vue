package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*") // 允许跨域访问
public class MaterialController {

    private static final Logger logger = Logger.getLogger(MaterialController.class.getName());

    @Autowired
    private MaterialService materialService;

    // 获取所有材料
    @GetMapping
    public ResponseEntity<List<Material>> getAllMaterials() {
        try {
            List<Material> materials = materialService.getAllMaterials();
            logger.info("Successfully retrieved " + materials.size() + " materials");
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            logger.severe("Error retrieving materials: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID获取材料
    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        try {
            Material material = materialService.getMaterialById(id);
            if (material != null) {
                return ResponseEntity.ok(material);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error retrieving material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 创建新材料
    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        try {
            Material savedMaterial = materialService.saveMaterial(material);
            return ResponseEntity.ok(savedMaterial);
        } catch (Exception e) {
            logger.severe("Error creating material: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 更新材料
    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @RequestBody Material materialDetails) {
        try {
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
        } catch (Exception e) {
            logger.severe("Error updating material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 删除材料
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        try {
            Material material = materialService.getMaterialById(id);
            if (material != null) {
                materialService.deleteMaterial(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
