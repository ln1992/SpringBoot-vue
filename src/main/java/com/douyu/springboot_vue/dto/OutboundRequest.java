package com.douyu.springboot_vue.dto;

/**
 * 出库请求DTO类
 */
public class OutboundRequest {
    private String size;
    private Long quantity;
    private String operator;

    // 默认构造函数
    public OutboundRequest() {}

    // 完整参数构造函数
    public OutboundRequest(String size, Long quantity, String operator) {
        this.size = size;
        this.quantity = quantity;
        this.operator = operator;
    }

    // getters and setters
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
