package com.boylegu.springboot_vue.entities;

public class MatterCompareItem {
    private Long mainItemCode;
    private Long subItemCode;
    private Long grandchildItemCode;
    private String __name__;
    private Long oldMatterId;
    private Long newMatterId;
    private CompareStatus status;

    // 构造函数
    public MatterCompareItem() {}

    public MatterCompareItem(Long mainItemCode, Long subItemCode, Long grandchildItemCode,
                             String __name__, Long oldMatterId, Long newMatterId, CompareStatus status) {
        this.mainItemCode = mainItemCode;
        this.subItemCode = subItemCode;
        this.grandchildItemCode = grandchildItemCode;
        this.__name__ = __name__;
        this.oldMatterId = oldMatterId;
        this.newMatterId = newMatterId;
        this.status = status;
    }

    // Getter和Setter方法
    public Long getMainItemCode() {
        return mainItemCode;
    }

    public void setMainItemCode(Long mainItemCode) {
        this.mainItemCode = mainItemCode;
    }

    public Long getSubItemCode() {
        return subItemCode;
    }

    public void setSubItemCode(Long subItemCode) {
        this.subItemCode = subItemCode;
    }

    public Long getGrandchildItemCode() {
        return grandchildItemCode;
    }

    public void setGrandchildItemCode(Long grandchildItemCode) {
        this.grandchildItemCode = grandchildItemCode;
    }

    public String get__name__() {
        return __name__;
    }

    public void set__name__(String __name__) {
        this.__name__ = __name__;
    }

    public Long getOldMatterId() {
        return oldMatterId;
    }

    public void setOldMatterId(Long oldMatterId) {
        this.oldMatterId = oldMatterId;
    }

    public Long getNewMatterId() {
        return newMatterId;
    }

    public void setNewMatterId(Long newMatterId) {
        this.newMatterId = newMatterId;
    }

    public CompareStatus getStatus() {
        return status;
    }

    public void setStatus(CompareStatus status) {
        this.status = status;
    }

    public enum CompareStatus {
        ADDED,      // 新增
        DELETED,    // 删除
        MODIFIED,   // 修改
        UNCHANGED   // 未变更
    }
}
