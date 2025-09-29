package com.douyu.springboot_vue.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 服装类 - 用于服装仓库管理系统
 */
@Entity
@Table(name = "clothing")
public class Clothing extends BaseEntity {

    // 服装名称
    @Column(name = "name", nullable = false)
    private String name;
    
    // 服装品牌
    @Column(name = "brand")
    private String brand;
    
    // 服装价格
    @Column(name = "price")
    private Double price;
    
    // 是否有效（上线/下线状态）
    // 修改为 Boolean 对象类型，允许 null 值
    @Column(name = "valid")
    private Boolean valid = true;
    
    // 各尺寸总入库数量 (S, M, L)
    @ElementCollection
    @CollectionTable(name = "clothing_total_quantity", joinColumns = @JoinColumn(name = "clothing_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "quantity")
    private Map<Size, Long> totalQuantityBySize = new HashMap<>();
    
    // 各尺寸当前库存数量 (S, M, L)
    @ElementCollection
    @CollectionTable(name = "clothing_current_stock", joinColumns = @JoinColumn(name = "clothing_id"))
    @MapKeyColumn(name = "size")
    @Column(name = "quantity")
    private Map<Size, Long> currentStockBySize = new HashMap<>();
    
    // 库存记录ID列表
    @ElementCollection
    @CollectionTable(name = "clothing_stock_record_ids", joinColumns = @JoinColumn(name = "clothing_id"))
    @Column(name = "stock_record_id")
    private List<String> stockRecordIds = new ArrayList<>();
    
    // 默认构造函数
    public Clothing() {}
    
    // 完整参数构造函数
    public Clothing(String name, String brand, Double price) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.valid = true;
    }
    
    // getter和setter方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }

    public Map<Size, Long> getTotalQuantityBySize() {
        return totalQuantityBySize;
    }

    public void setTotalQuantityBySize(Map<Size, Long> totalQuantityBySize) {
        this.totalQuantityBySize = totalQuantityBySize;
    }
    
    public Map<Size, Long> getCurrentStockBySize() {
        return currentStockBySize;
    }
    
    public void setCurrentStockBySize(Map<Size, Long> currentStockBySize) {
        this.currentStockBySize = currentStockBySize;
    }
    
    public List<String> getStockRecordIds() {
        return stockRecordIds;
    }
    
    public void setStockRecordIds(List<String> stockRecordIds) {
        this.stockRecordIds = stockRecordIds;
    }

    // 添加库存记录ID
    public void addStockRecordId(String stockRecordId) {
        this.stockRecordIds.add(stockRecordId);
    }

    // 添加特定尺寸的总入库数量
    public void addTotalQuantity(Size size, long quantity) {
        long current = this.totalQuantityBySize.getOrDefault(size, 0L);
        this.totalQuantityBySize.put(size, current + quantity);
    }
    
    // 获取特定尺寸的总入库数量
    public Long getTotalQuantityBySize(Size size) {
        return this.totalQuantityBySize.getOrDefault(size, 0L);
    }
    
    // 添加特定尺寸的当前库存数量
    public void addCurrentStock(Size size, long quantity) {
        long current = this.currentStockBySize.getOrDefault(size, 0L);
        this.currentStockBySize.put(size, current + quantity);
    }
    
    // 获取特定尺寸的当前库存数量
    public Long getCurrentStockBySize(Size size) {
        return this.currentStockBySize.getOrDefault(size, 0L);
    }


    /**
     * 获取所有尺寸的出库数量映射
     * 出库数量 = 总入库数量 - 当前库存数量
     */
    @JsonIgnore
    public Map<Size, Long> getOutboundQuantityBySize() {
        Map<Size, Long> outboundMap = new HashMap<>();
        for (Size size : Size.values()) {
            Long total = this.totalQuantityBySize.getOrDefault(size, 0L);
            Long current = this.currentStockBySize.getOrDefault(size, 0L);
            outboundMap.put(size, Math.max(0L, total - current));
        }
        return outboundMap;
    }
    
    // 获取特定尺寸的出库数量
    public Long getOutboundQuantityBySize(Size size) {
        Long total = this.totalQuantityBySize.getOrDefault(size, 0L);
        Long current = this.currentStockBySize.getOrDefault(size, 0L);
        return total - current;
    }
    
    // 入库操作
    public void inbound(Size size, long quantity, String operator) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        // 更新总入库数量
        addTotalQuantity(size, quantity);
        
        // 更新当前库存
        addCurrentStock(size, quantity);
        
        // 注意：这里不再创建库存记录对象，只执行业务逻辑
        // 记录的创建将由Service层处理
    }
    
    // 出库操作
    public boolean outbound(Size size, long quantity, String operator) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("出库数量必须大于0");
        }

        // 检查库存是否充足
        if (getCurrentStockBySize(size) < quantity) {
            return false; // 库存不足
        }
        
        // 更新当前库存（出库为负数）
        addCurrentStock(size, -quantity);
        
        // 注意：这里不再创建库存记录对象，只执行业务逻辑
        // 记录的创建将由Service层处理
        return true;
    }
    
    @Override
    public String toString() {
        return "Clothing{" +
                "name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", valid=" + valid +
                ", totalQuantityBySize=" + totalQuantityBySize +
                ", currentStockBySize=" + currentStockBySize +
                '}';
    }

    /**
     * 服装尺码枚举
     */
    public enum Size {
        S("S"),
        M("M"),
        L("L");

        private final String code;

        Size(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Size valueOfCode(String code) {
            for (Size size : values()) {
                if (size.getCode().equals(code)) {
                    return size;
                }
            }
            throw new IllegalArgumentException("No matching size for code: " + code);
        }

        @Override
        public String toString() {
            return code;
        }
    }

}