package com.douyu.springboot_vue.dto;

/**
 * 入库请求DTO类
 */
public class InboundRequest {
    private String size;
    private Long quantity;
    private String operator;

    // 默认构造函数
    public InboundRequest() {}

    // 完整参数构造函数
    public InboundRequest(String size, Long quantity, String operator) {
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
