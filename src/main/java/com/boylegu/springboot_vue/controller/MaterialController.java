// src/main/java/com/boylegu/springboot_vue/controller/MaterialController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<?> createMaterial(@RequestBody @Valid Material material, BindingResult bindingResult) {
        // 处理验证错误
        if (bindingResult.hasErrors()) {
            logger.severe("Validation error creating material: " + bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("验证失败: " + bindingResult.getAllErrors());
        }

        try {
            logger.info("Creating material with data: " + material);
            Material savedMaterial = materialService.saveMaterial(material);
            logger.info("Material created successfully with ID: " + savedMaterial.getId());
            return ResponseEntity.ok(savedMaterial);
        } catch (Exception e) {
            logger.severe("Error creating material: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("创建材料失败: " + e.getMessage());
        }
    }

    // 更新材料
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable Long id, @RequestBody @Valid Material materialDetails, BindingResult bindingResult) {
        // 处理验证错误
        if (bindingResult.hasErrors()) {
            logger.severe("Validation error updating material: " + bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("验证失败: " + bindingResult.getAllErrors());
        }

        try {
            logger.info("Updating material ID " + id + " with data: " + materialDetails);
            Material material = materialService.getMaterialById(id);
            if (material != null) {
                // 更新字段
                material.setMaterialDetail(materialDetails.getMaterialDetail());
                material.setReviewPoint(materialDetails.getReviewPoint());
                material.setAutoApprovalCriteria(materialDetails.getAutoApprovalCriteria());
                material.setShared(materialDetails.getShared());
                material.setMaterialSource(materialDetails.getMaterialSource());
                material.setProcessingMethodAndInfoAccess(materialDetails.getProcessingMethodAndInfoAccess());
                material.setEligibleForPromise(materialDetails.getEligibleForPromise());
                material.setIsValid(materialDetails.getIsValid());

                Material updatedMaterial = materialService.saveMaterial(material);
                logger.info("Material updated successfully with ID: " + updatedMaterial.getId());
                return ResponseEntity.ok(updatedMaterial);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error updating material with id " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("更新材料失败: " + e.getMessage());
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

    // 上线材料
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateMaterial(@PathVariable Long id) {
        try {
            boolean success = materialService.activateMaterial(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error activating material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("上线材料失败: " + e.getMessage());
        }
    }

    // 下线材料
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateMaterial(@PathVariable Long id) {
        try {
            boolean success = materialService.deactivateMaterial(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deactivating material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).body("下线材料失败: " + e.getMessage());
        }
    }

    @GetMapping("/search/valid")
    public ResponseEntity<List<Material>> getValidMaterials(@RequestParam Boolean isValid) {
        try {
            List<Material> materials = materialService.getMaterialsByIsValid(isValid);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            logger.severe("Error retrieving materials by valid status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
