package com.douyu.springboot_vue.entities;

import javax.persistence.*;

import com.douyu.springboot_vue.entities.BaseEntity;
import com.douyu.springboot_vue.entities.Clothing;

/**
 * 服装库存记录类 - 用于记录服装各种数量变化的历史记录
 */
@Entity
@Table(name = "clothing_stock_record")
public class ClothingStockRecord extends BaseEntity {

    // 关联的服装实体ID
    @Column(name = "clothing_id", nullable = false)
    private Long clothingId;

    // 关联的服装名称
    @Column(name = "clothing_name")
    private String clothingName;

    // 尺寸 (S, M, L)
    @Column(name = "size", nullable = false)
    private String size;

    // 操作类型 (INBOUND: 入库, OUTBOUND: 出库)
    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockOperationType operationType;

    // 操作数量
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    // 操作前库存
    @Column(name = "previous_stock")
    private Long previousStock;

    // 操作后库存
    @Column(name = "current_stock")
    private Long currentStock;

    // 操作员
    @Column(name = "operator")
    private String operator;

    // 默认构造函数
    public ClothingStockRecord() {}

    // 完整参数构造函数
    public ClothingStockRecord(Long clothingId, String clothingName, Clothing.Size size, StockOperationType operationType,
                               Long quantity, Long previousStock, Long currentStock, String operator) {
        this.clothingId = clothingId;
        this.clothingName = clothingName;
        this.size = size.getCode();
        this.operationType = operationType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.operator = operator;
        this.setValid(true);
    }

    // getter和setter方法
    public Long getClothingId() {
        return clothingId;
    }

    public void setClothingId(Long clothingId) {
        this.clothingId = clothingId;
    }

    public String getClothingName() {
        return clothingName;
    }

    public void setClothingName(String clothingName) {
        this.clothingName = clothingName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public StockOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(StockOperationType operationType) {
        this.operationType = operationType;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Long previousStock) {
        this.previousStock = previousStock;
    }

    public Long getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Long currentStock) {
        this.currentStock = currentStock;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "ClothingStockRecord{" +
                "clothingId=" + clothingId +
                ", clothingName='" + clothingName + '\'' +
                ", size='" + size + '\'' +
                ", operationType=" + operationType +
                ", quantity=" + quantity +
                ", previousStock=" + previousStock +
                ", currentStock=" + currentStock +
                ", operator='" + operator + '\'' +
                '}';
    }

    /**
     * 库存操作类型枚举
     */
    public enum StockOperationType {
        /**
         * 入库操作
         */
        INBOUND("入库"),

        /**
         * 出库操作
         */
        OUTBOUND("出库");

        private final String description;

        StockOperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据描述获取枚举值
         * @param description 描述
         * @return 对应的枚举值
         */
        public static StockOperationType fromDescription(String description) {
            for (StockOperationType type : StockOperationType.values()) {
                if (type.description.equals(description)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的操作类型: " + description);
        }
    }
}
