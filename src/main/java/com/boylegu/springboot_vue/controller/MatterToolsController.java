// src/main/java/com/boylegu/springboot_vue/controller/MatterToolsController.java
package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.entities.MatterCompareItem;
import com.boylegu.springboot_vue.service.MatterExportService;
import com.boylegu.springboot_vue.service.MatterService;
import com.boylegu.springboot_vue.service.MatterVersionCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/matters/tools")
@CrossOrigin(origins = "*")
public class MatterToolsController {

    private static final Logger logger = Logger.getLogger(MatterToolsController.class.getName());

    @Autowired
    private MatterService matterService;

    @Autowired
    private MatterExportService matterExportService;

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

    /**
     * 对比两个事项对象，返回差异字段的详细信息
     * @param oldMatterId 旧版本事项ID
     * @param newMatterId 新版本事项ID
     * @return 包含差异字段信息的Map，结构为 {old:{"fieldName":oldValue}, new:{"fieldName":newValue}}
     */
    @GetMapping("/compare-matters")
    public ResponseEntity<Map<String, Map<String, Object>>> compareMatters(
            @RequestParam(required = false) Long oldMatterId,
            @RequestParam(required = false) Long newMatterId) {
        try {
            logger.info("Comparing matters: " + oldMatterId + " and " + newMatterId);
            Map<String, Map<String, Object>> compareResult = matterVersionCompareService.compareMattersById(oldMatterId, newMatterId);
            return ResponseEntity.ok(compareResult);
        } catch (Exception e) {
            logger.severe("Error comparing matters: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 导出指定版本的有效事项目录
     */
    @GetMapping("/export/catalog")
    public ResponseEntity<byte[]> exportMattersCatalogByVersion(@RequestParam Long version) {
        logger.info("收到导出事项目录请求，版本号: " + version);

        try {
            // 检查版本参数
            if (version == null || version <= 0) {
                logger.warning("无效的版本号: " + version);
                return ResponseEntity.badRequest().build();
            }

            List<Matter> matters = matterService.findMattersByVersionAndValid(version);
            logger.info("找到 " + matters.size() + " 个有效事项");

            byte[] documentBytes = matterExportService.exportMattersCatalogByVersion(version);
            logger.info("成功导出版本 " + version + " 的事项目录，文件大小: " + documentBytes.length + " 字节");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "事项目录_v" + version + ".docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(documentBytes);
        } catch (Exception e) {
            logger.severe("导出事项目录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 导出指定版本的有效事项文档
     */
    @GetMapping("/export/documents")
    public ResponseEntity<byte[]> exportMattersDocumentsByVersion(@RequestParam Long version) {
        logger.info("收到导出事项文档请求，版本号: " + version);

        try {
            // 检查版本参数
            if (version == null || version <= 0) {
                logger.warning("无效的版本号: " + version);
                return ResponseEntity.badRequest().build();
            }

            List<Matter> matters = matterService.findMattersByVersionAndValid(version);
            logger.info("找到 " + matters.size() + " 个有效事项");

            byte[] documentBytes = matterExportService.exportMattersDocumentsByVersion(version);
            logger.info("成功导出版本 " + version + " 的事项文档，文件大小: " + documentBytes.length + " 字节");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "事项文档_v" + version + ".docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(documentBytes);
        } catch (Exception e) {
            logger.severe("导出事项文档失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}
