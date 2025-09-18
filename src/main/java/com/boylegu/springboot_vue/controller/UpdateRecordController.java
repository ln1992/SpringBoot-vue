package com.boylegu.springboot_vue.controller;

import com.boylegu.springboot_vue.entities.UpdateRecord;
import com.boylegu.springboot_vue.service.UpdateRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/update-records")
@CrossOrigin(origins = "*") // 允许跨域访问
public class UpdateRecordController {

    private static final Logger logger = Logger.getLogger(UpdateRecordController.class.getName());

    @Autowired
    private UpdateRecordService updateRecordService;

    // 获取所有更新记录
    @GetMapping
    public ResponseEntity<List<UpdateRecord>> getAllUpdateRecords(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false, defaultValue = "createdTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        try {
            List<UpdateRecord> records = updateRecordService.findUpdateRecordsWithFilters(
                    entityName, entityType, operationType, sortBy, sortDirection);
            logger.info("Successfully retrieved " + records.size() + " update records");
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.severe("Error retrieving update records: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID获取更新记录
    @GetMapping("/{id}")
    public ResponseEntity<UpdateRecord> getUpdateRecordById(@PathVariable Long id) {
        try {
            UpdateRecord record = updateRecordService.findUpdateRecordById(id);
            if (record != null) {
                return ResponseEntity.ok(record);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Error retrieving update record with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据ID删除更新记录
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpdateRecord(@PathVariable Long id) {
        try {
            updateRecordService.deleteUpdateRecordById(id);
            logger.info("Successfully deleted update record with id: " + id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("Error deleting update record with id " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据实体类型获取更新记录
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<UpdateRecord>> getUpdateRecordsByEntityType(@PathVariable String entityType) {
        try {
            List<UpdateRecord> records = updateRecordService.findUpdateRecordsByEntityType(entityType);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.severe("Error retrieving update records by entity type " + entityType + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据操作类型获取更新记录
    @GetMapping("/operation-type/{operationType}")
    public ResponseEntity<List<UpdateRecord>> getUpdateRecordsByOperationType(@PathVariable String operationType) {
        try {
            UpdateRecord.OperationType type = UpdateRecord.OperationType.valueOf(operationType);
            List<UpdateRecord> records = updateRecordService.findUpdateRecordsByOperationType(type);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.severe("Error retrieving update records by operation type " + operationType + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 根据操作用户获取更新记录
    @GetMapping("/operator/{operator}")
    public ResponseEntity<List<UpdateRecord>> getUpdateRecordsByOperator(@PathVariable String operator) {
        try {
            List<UpdateRecord> records = updateRecordService.findUpdateRecordsByOperator(operator);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.severe("Error retrieving update records by operator " + operator + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    // 根据实体ID获取更新记录
    @GetMapping("/entity-id/{entityId}")
    public ResponseEntity<List<UpdateRecord>> getUpdateRecordsByEntityId(@PathVariable Long entityId) {
        try {
            List<UpdateRecord> records = updateRecordService.findUpdateRecordsByEntityId(entityId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.severe("Error retrieving update records by entity id " + entityId + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}