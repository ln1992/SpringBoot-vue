package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MaterialService;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*") // 允许跨域访问
public class MaterialController {

    private static final Logger logger = Logger.getLogger(MaterialController.class.getName());

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MatterService matterService;

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
    public ResponseEntity<?> createMatter(@RequestBody @Valid Matter matter) {
        try {
            logger.info("Creating matter with data: " + matter);
            if (matter.getMaterialIds() != null) {
                logger.info("Material IDs to validate: " + matter.getMaterialIds());
            }

            Matter savedMatter = matterService.saveMatter(matter);
            logger.info("Matter created successfully with ID: " + savedMatter.getId());
            return ResponseEntity.ok(savedMatter);
        } catch (Exception e) {
            logger.severe("Error creating matter: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("创建事项失败: " + e.getMessage());
        }
    }

    // 更新材料
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMatter(@PathVariable Long id, @RequestBody @Valid Matter matterDetails) {
        try {
            logger.info("Updating matter ID " + id + " with data: " + matterDetails);
            if (matterDetails.getMaterialIds() != null) {
                logger.info("Material IDs to validate: " + matterDetails.getMaterialIds());
            }

            Matter matter = matterService.getMatterById(id);
            if (matter != null) {
                // 更新字段
                matter.setMainItemName(matterDetails.getMainItemName());
                matter.setSubItemName(matterDetails.getSubItemName());
                matter.setGrandchildItemName(matterDetails.getGrandchildItemName());
                matter.setBasisList(matterDetails.getBasisList());
                matter.setMaterialIds(matterDetails.getMaterialIds());
                matter.setLegalTimeLimit(matterDetails.getLegalTimeLimit());
                matter.setCommittedTimeLimit(matterDetails.getCommittedTimeLimit());
                matter.setApprovalLevel(matterDetails.getApprovalLevel());
                matter.setProvincialDepartmentOffice(matterDetails.getProvincialDepartmentOffice());
                matter.setIsValid(matterDetails.getIsValid());

                Matter updatedMatter = matterService.saveMatter(matter);
                logger.info("Matter updated successfully with ID: " + updatedMatter.getId());
                return ResponseEntity.ok(updatedMatter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error updating matter with id " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("更新事项失败: " + e.getMessage());
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
    public ResponseEntity<Void> activateMaterial(@PathVariable Long id) {
        try {
            boolean success = materialService.activateMaterial(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error activating material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 下线材料
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateMaterial(@PathVariable Long id) {
        try {
            boolean success = materialService.deactivateMaterial(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deactivating material with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
