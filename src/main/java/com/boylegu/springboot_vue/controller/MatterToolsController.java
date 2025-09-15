// src/main/java/com/boylegu/springboot_vue/controller/MatterToolsController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.MatterCompareItem;
import com.boylegu.springboot_vue.service.MatterVersionCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/matters/tools")
@CrossOrigin(origins = "*")
public class MatterToolsController {

    private static final Logger logger = Logger.getLogger(MatterToolsController.class.getName());

    @Autowired
    private MatterVersionCompareService matterVersionCompareService;

    /**
     * 比较两个版本的事项数据
     */
    @GetMapping("/compare-versions")
    public ResponseEntity<List<MatterCompareItem>> compareMatterVersions(
            @RequestParam Long version1,
            @RequestParam Long version2) {
        System.out.println("对比version1: " + version1 + ", version2: " + version2);
        try {
            logger.info("Comparing matter versions: " + version1 + " and " + version2);
            List<MatterCompareItem> compareResult = matterVersionCompareService.compareVersions(version1, version2);
            return ResponseEntity.ok(compareResult);
        } catch (Exception e) {
            logger.severe("Error comparing matter versions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
