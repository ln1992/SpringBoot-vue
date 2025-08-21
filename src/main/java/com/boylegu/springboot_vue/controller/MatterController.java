// src/main/java/com/boylegu/springboot_vue/controller/MatterController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matters")
@CrossOrigin(origins = "*") // 允许跨域访问
public class MatterController {

    private static final Logger logger = Logger.getLogger(MatterController.class.getName());

    @Autowired
    private MatterService matterService;

    // 获取所有事项
    @GetMapping
    public ResponseEntity<List<Matter>> getAllMatters() {
        try {
            List<Matter> matters = matterService.getAllMatters();
            logger.info("Successfully retrieved " + matters.size() + " matters");
            return ResponseEntity.ok(matters);
        } catch (Exception e) {
            logger.severe("Error retrieving matters: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID获取事项
    @GetMapping("/{id}")
    public ResponseEntity<Matter> getMatterById(@PathVariable Long id) {
        try {
            Matter matter = matterService.getMatterById(id);
            if (matter != null) {
                return ResponseEntity.ok(matter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error retrieving matter with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 创建新事项
    @PostMapping
    public ResponseEntity<?> createMatter(@RequestBody @Valid Matter matter) {
        try {
            Matter savedMatter = matterService.saveMatter(matter);
            return ResponseEntity.ok(savedMatter);
        } catch (ConstraintViolationException e) {
            // 处理验证错误
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            String errorMessage = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));

            logger.severe("Validation error creating matter: " + errorMessage);
            return ResponseEntity.badRequest().body("验证失败: " + errorMessage);
        } catch (Exception e) {
            logger.severe("Error creating matter: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("创建事项失败: " + e.getMessage());
        }
    }

    // 更新事项
    // 更新事项
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMatter(@PathVariable Long id, @RequestBody @Valid Matter matterDetails, BindingResult bindingResult) {
        // 处理验证错误
        if (bindingResult.hasErrors()) {
            logger.severe("Validation error updating matter: " + bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("验证失败: " + bindingResult.getAllErrors());
        }

        try {
            logger.info("Updating matter ID " + id + " with data: " + matterDetails);
            Matter matter = matterService.getMatterById(id);
            if (matter != null) {
                // 更新字段
                matter.setMainItemCode(matterDetails.getMainItemCode()); // 新增
                matter.setSubItemCode(matterDetails.getSubItemCode());   // 新增
                matter.setGrandchildItemCode(matterDetails.getGrandchildItemCode()); // 新增
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


    // 删除事项
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatter(@PathVariable Long id) {
        try {
            Matter matter = matterService.getMatterById(id);
            if (matter != null) {
                matterService.deleteMatter(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting matter with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据主项名称查询事项列表
    @GetMapping("/search/main-item")
    public ResponseEntity<List<Matter>> getMattersByMainItemName(@RequestParam String mainItemName) {
        try {
            List<Matter> matters = matterService.getMattersByMainItemName(mainItemName);
            return ResponseEntity.ok(matters);
        } catch (Exception e) {
            logger.severe("Error retrieving matters by main item name: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据审批层级查询事项列表 - 修复：接收字符串参数并转换为枚举
    @GetMapping("/search/approval-level")
    public ResponseEntity<List<Matter>> getMattersByApprovalLevel(@RequestParam String approvalLevel) {
        try {
            Matter.ApprovalLevel level = Matter.ApprovalLevel.valueOf(approvalLevel);
            List<Matter> matters = matterService.getMattersByApprovalLevel(level);
            return ResponseEntity.ok(matters);
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid approval level: " + approvalLevel);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Error retrieving matters by approval level: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据省厅对口指导处室（单位）查询事项列表 - 修复：接收字符串参数并转换为枚举
    @GetMapping("/search/provincial-department-office")
    public ResponseEntity<List<Matter>> getMattersByProvincialDepartmentOffice(
            @RequestParam String provincialDepartmentOffice) {
        try {
            Matter.ProvincialDepartmentOffice office = Matter.ProvincialDepartmentOffice.valueOf(provincialDepartmentOffice);
            List<Matter> matters = matterService.getMattersByProvincialDepartmentOffice(office);
            return ResponseEntity.ok(matters);
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid provincial department office: " + provincialDepartmentOffice);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Error retrieving matters by provincial department office: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 激活事项（上线）
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateMatter(@PathVariable Long id) {
        Matter activatedMatter = matterService.activateMatter(id);
        if (activatedMatter != null) {
            return ResponseEntity.ok(activatedMatter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 停用事项（下线）
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateMatter(@PathVariable Long id) {
        Matter deactivatedMatter = matterService.deactivateMatter(id);
        if (deactivatedMatter != null) {
            return ResponseEntity.ok(deactivatedMatter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
