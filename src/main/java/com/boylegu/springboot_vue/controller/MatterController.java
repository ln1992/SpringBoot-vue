package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

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
    public ResponseEntity<Matter> createMatter(@RequestBody Matter matter) {
        try {
            Matter savedMatter = matterService.saveMatter(matter);
            return ResponseEntity.ok(savedMatter);
        } catch (Exception e) {
            logger.severe("Error creating matter: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 更新事项
    @PutMapping("/{id}")
    public ResponseEntity<Matter> updateMatter(@PathVariable Long id, @RequestBody Matter matterDetails) {
        try {
            Matter matter = matterService.getMatterById(id);
            if (matter != null) {
                matter.setMainItemName(matterDetails.getMainItemName());
                matter.setSubItemName(matterDetails.getSubItemName());
                matter.setGrandchildItemName(matterDetails.getGrandchildItemName());
                matter.setBasisList(matterDetails.getBasisList());
                matter.setMaterialIds(matterDetails.getMaterialIds());
                matter.setLegalTimeLimit(matterDetails.getLegalTimeLimit());
                matter.setCommittedTimeLimit(matterDetails.getCommittedTimeLimit());
                matter.setApprovalLevel(matterDetails.getApprovalLevel());
                matter.setProvincialDepartmentOffice(matterDetails.getProvincialDepartmentOffice());

                Matter updatedMatter = matterService.saveMatter(matter);
                return ResponseEntity.ok(updatedMatter);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error updating matter with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
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

    // 在你的控制器类中添加以下方法

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
