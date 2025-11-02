package com.douyu.springboot_vue.controller;

import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.service.ClothingStockRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服装库存记录控制器
 */
@RestController
@RequestMapping("/api/clothing-stock-records")
@CrossOrigin(origins = "*")
public class ClothingStockRecordController {

    private final ClothingStockRecordService clothingStockRecordService;

    @Autowired
    public ClothingStockRecordController(ClothingStockRecordService clothingStockRecordService) {
        this.clothingStockRecordService = clothingStockRecordService;
    }

    /**
     * 获取所有库存记录
     * @return 库存记录列表
     */
    @GetMapping
    public ResponseEntity<List<ClothingStockRecord>> getAllStockRecords() {
        List<ClothingStockRecord> stockRecords = clothingStockRecordService.getAllStockRecords();
        return new ResponseEntity<>(stockRecords, HttpStatus.OK);
    }

    /**
     * 根据ID获取库存记录
     * @param id 库存记录ID
     * @return 库存记录对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClothingStockRecord> getStockRecordById(@PathVariable Long id) {
        Optional<ClothingStockRecord> stockRecord = clothingStockRecordService.getStockRecordById(id);
        if (stockRecord.isPresent()) {
            return new ResponseEntity<>(stockRecord.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 根据服装ID获取库存记录
     * @param clothingId 服装ID
     * @return 库存记录列表
     */
    @GetMapping("/clothing/{clothingId}")
    public ResponseEntity<List<ClothingStockRecord>> getStockRecordsByClothingId(@PathVariable Long clothingId) {
        List<ClothingStockRecord> stockRecords = clothingStockRecordService.getStockRecordsByClothingId(clothingId);
        return new ResponseEntity<>(stockRecords, HttpStatus.OK);
    }

    /**
     * 创建新的库存记录
     * @param stockRecord 库存记录对象
     * @return 创建后的库存记录
     */
    @PostMapping
    public ResponseEntity<ClothingStockRecord> createStockRecord(@RequestBody ClothingStockRecord stockRecord) {
        ClothingStockRecord savedStockRecord = clothingStockRecordService.saveStockRecord(stockRecord);
        return new ResponseEntity<>(savedStockRecord, HttpStatus.CREATED);
    }

    /**
     * 更新库存记录
     * @param id 库存记录ID
     * @param stockRecord 更新的库存记录对象
     * @return 更新后的库存记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClothingStockRecord> updateStockRecord(@PathVariable Long id, @RequestBody ClothingStockRecord stockRecord) {
        Optional<ClothingStockRecord> existingStockRecord = clothingStockRecordService.getStockRecordById(id);
        if (existingStockRecord.isPresent()) {
            stockRecord.setId(id);
            ClothingStockRecord updatedStockRecord = clothingStockRecordService.saveStockRecord(stockRecord);
            return new ResponseEntity<>(updatedStockRecord, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 删除库存记录
     * @param id 库存记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockRecord(@PathVariable Long id) {
        Optional<ClothingStockRecord> existingStockRecord = clothingStockRecordService.getStockRecordById(id);
        if (existingStockRecord.isPresent()) {
            clothingStockRecordService.deleteStockRecord(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 下线库存记录
     * @param id 库存记录ID
     * @return 操作结果
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateStockRecord(@PathVariable Long id) {
        try {
            Optional<ClothingStockRecord> result = clothingStockRecordService.deactivateStockRecord(id);
            if (result.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "库存记录下线成功");
                response.put("data", result.get());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的库存记录");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "下线库存记录失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 上线库存记录
     * @param id 库存记录ID
     * @return 操作结果
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateStockRecord(@PathVariable Long id) {
        try {
            Optional<ClothingStockRecord> result = clothingStockRecordService.activateStockRecord(id);
            if (result.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "库存记录上线成功");
                response.put("data", result.get());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的库存记录");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "上线库存记录失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除已下线的库存记录
     * @param id 库存记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/delete-deactivated")
    public ResponseEntity<?> deleteDeactivatedStockRecord(@PathVariable Long id) {
        try {
            Optional<ClothingStockRecord> existingStockRecord = clothingStockRecordService.getStockRecordById(id);
            if (existingStockRecord.isPresent()) {
                ClothingStockRecord stockRecord = existingStockRecord.get();
                // 检查记录是否已下线
                if (stockRecord.getValid()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "不能删除已上线的库存记录，请先下线再删除");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                
                // 删除记录
                clothingStockRecordService.deleteStockRecord(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "已下线的库存记录删除成功");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的库存记录");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除库存记录失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}