package com.douyu.springboot_vue.controller;

import com.douyu.springboot_vue.dto.InboundRequest;
import com.douyu.springboot_vue.dto.OutboundRequest;
import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.service.ClothingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服装控制器
 */
@RestController
@RequestMapping("/api/clothings")
@CrossOrigin(origins = "*")
public class ClothingController {

    private final ClothingService clothingService;

    @Autowired
    public ClothingController(ClothingService clothingService) {
        this.clothingService = clothingService;
    }

    /**
     * 获取所有服装
     * @return 服装列表
     */
    @GetMapping
    public ResponseEntity<?> getAllClothings() {
        try {
            List<Clothing> clothings = clothingService.getAllClothings();
            return new ResponseEntity<>(clothings, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取服装列表失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据ID获取服装
     * @param id 服装ID
     * @return 服装对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClothingById(@PathVariable Long id) {
        try {
            Optional<Clothing> clothing = clothingService.getClothingById(id);
            if (clothing.isPresent()) {
                return new ResponseEntity<>(clothing.get(), HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的服装");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建新的服装
     * @param clothing 服装对象
     * @return 创建后的服装
     */
    @PostMapping
    public ResponseEntity<?> createClothing(@RequestBody Clothing clothing) {
        try {
            Clothing savedClothing = clothingService.createClothing(clothing);
            return new ResponseEntity<>(savedClothing, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新服装
     * @param id 服装ID
     * @param clothing 更新的服装对象
     * @return 更新后的服装
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClothing(@PathVariable Long id, @RequestBody Clothing clothing) {
        try {
            Optional<Clothing> existingClothing = clothingService.getClothingById(id);
            if (existingClothing.isPresent()) {
                try {
                    Clothing updatedClothing = clothingService.updateClothing(id, clothing);
                    return new ResponseEntity<>(updatedClothing, HttpStatus.OK);
                } catch (Exception e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "更新服装失败: " + e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的服装");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除服装
     * @param id 服装ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClothing(@PathVariable Long id) {
        try {
            Optional<Clothing> existingClothing = clothingService.getClothingById(id);
            if (existingClothing.isPresent()) {
                // 检查服装是否已下线，只有下线的服装才能删除
                if (existingClothing.get().getValid()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "不能删除上线状态的服装，请先下线再删除");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 不能删除上线的服装
                }
                try {
                    clothingService.deleteClothing(id);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "服装删除成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (Exception e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "删除服装失败: " + e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的服装");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 上线服装
     * @param id 服装ID
     * @return 操作结果
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateClothing(@PathVariable Long id) {
        try {
            Optional<Clothing> existingClothing = clothingService.getClothingById(id);
            if (existingClothing.isPresent()) {
                try {
                    clothingService.activateClothing(id);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "服装上线成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (Exception e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "服装上线失败: " + e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的服装");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 下线服装
     * @param id 服装ID
     * @return 操作结果
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateClothing(@PathVariable Long id) {
        try {
            Optional<Clothing> existingClothing = clothingService.getClothingById(id);
            if (existingClothing.isPresent()) {
                try {
                    clothingService.deactivateClothing(id);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "服装下线成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (Exception e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "服装下线失败: " + e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到ID为 " + id + " 的服装");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询服装失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 入库操作
     * @param id 服装ID
     * @param request 入库请求参数
     * @return 操作结果
     */
    @PostMapping("/{id}/inbound")
    public ResponseEntity<?> inboundClothing(@PathVariable Long id, @RequestBody InboundRequest request) {
        try {
            String sizeCode = request.getSize();
            Long quantity = request.getQuantity();
            String operator = request.getOperator();

            // 验证参数
            if (sizeCode == null || quantity == null || operator == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "缺少必要的参数: size, quantity, operator");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 验证数量参数
            if (quantity <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "数量必须大于0");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 转换尺寸
            Clothing.Size size;
            try {
                size = Clothing.Size.valueOfCode(sizeCode);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "无效的尺寸代码: " + sizeCode);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 执行入库操作
            try {
                boolean success = clothingService.inboundClothing(id, size, quantity, operator);

                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "入库操作成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    response.put("success", false);
                    response.put("message", "入库操作失败，服装不存在");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "入库操作失败: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理入库请求失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 出库操作
     * @param id 服装ID
     * @param request 出库请求参数
     * @return 操作结果
     */
    @PostMapping("/{id}/outbound")
    public ResponseEntity<?> outboundClothing(@PathVariable Long id, @RequestBody OutboundRequest request) {
        try {
            String sizeCode = request.getSize();
            Long quantity = request.getQuantity();
            String operator = request.getOperator();

            // 验证参数
            if (sizeCode == null || quantity == null || operator == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "缺少必要的参数: size, quantity, operator");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 验证数量参数
            if (quantity <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "数量必须大于0");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 转换尺寸
            Clothing.Size size;
            try {
                size = Clothing.Size.valueOfCode(sizeCode);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "无效的尺寸代码: " + sizeCode);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 执行出库操作
            try {
                boolean success = clothingService.outboundClothing(id, size, quantity, operator);

                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "出库操作成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else if (!clothingService.getClothingById(id).isPresent()) {
                    response.put("success", false);
                    response.put("message", "出库操作失败，服装不存在");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                } else {
                    response.put("success", false);
                    response.put("message", "出库操作失败，库存不足");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "出库操作失败: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理出库请求失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据库存记录恢复库存
     * @param id 服装ID
     * @return 操作结果
     */
    @PostMapping("/{id}/restore-stock")
    public ResponseEntity<?> restoreStockFromRecords(@PathVariable Long id) {
        try {
            boolean success = clothingService.restoreStockFromRecords(id);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "库存恢复成功");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "库存恢复失败，服装不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "库存恢复失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
