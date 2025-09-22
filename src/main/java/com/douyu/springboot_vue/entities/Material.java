// src/main/java/com/boylegu/springboot_vue/entities/Material.java
package com.douyu.springboot_vue.entities;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * 材料类 - 用于描述各种申请或办理材料的信息
 */
@Entity
@Table(name = "material",
        uniqueConstraints = @UniqueConstraint(columnNames = {"material_detail", "version"}))
public class Material extends BaseEntity {
    // 材料明细
    @Column(name = "material_detail", nullable = false)
    @NotBlank(message = "材料明细不能为空")
    private String materialDetail;

    // 审核要点
    @Column(name = "review_point")
    private String reviewPoint;

    // "智能秒批"判断标准
    @Column(name = "auto_approval_criteria")
    private String autoApprovalCriteria;

    // 是否共享
    @Column(name = "is_shared")
    private Boolean isShared = false;

    // 材料来源
    @Column(name = "material_source")
    @Enumerated(EnumType.STRING)
    private MaterialSource materialSource;

    // 办理方式及材料信息获取方式说明
    @Column(name = "processing_method_and_info_access")
    @Enumerated(EnumType.STRING)
    private ProcessingMethodAndInfoAccess processingMethodAndInfoAccess;

    // 是否适用告知承诺
    @Column(name = "is_eligible_for_promise")
    private Boolean isEligibleForPromise = false;

    // 默认构造函数
    public Material() {}

    // 完整参数构造函数
    public Material(String materialDetail, String reviewPoint, String autoApprovalCriteria,
                    Boolean isShared, MaterialSource materialSource, ProcessingMethodAndInfoAccess processingMethodAndInfoAccess,
                    Boolean isEligibleForPromise) {
        this.materialDetail = materialDetail;
        this.reviewPoint = reviewPoint;
        this.autoApprovalCriteria = autoApprovalCriteria;
        this.isShared = isShared;
        this.materialSource = materialSource;
        this.processingMethodAndInfoAccess = processingMethodAndInfoAccess;
        this.isEligibleForPromise = isEligibleForPromise;
    }

    @Override
    public void setVersion(Long version) {
        if (version != null) {
            super.setVersion(version);
        } else {
            // 如果传入 null，则保持当前值或设置默认值
            if (super.getVersion() == null) {
                super.setVersion(1L);
            }
        }
        updateName(); // 自动更新名称
    }

    public void setMaterialDetail(String materialDetail) {
        this.materialDetail = materialDetail;
        updateName(); // 自动更新名称
    }

    public void updateName() {
        if (this.materialDetail != null && this.getVersion() != null) {
            this.set__name__(this.materialDetail + "_" + this.getVersion());
        } else if (this.materialDetail != null) {
            this.set__name__(this.materialDetail);
        } else {
            this.set__name__(null);
        }
    }

    // getter和setter方法
    public String getMaterialDetail() {
        return materialDetail;
    }

    public String getReviewPoint() {
        return reviewPoint;
    }

    public void setReviewPoint(String reviewPoint) {
        this.reviewPoint = reviewPoint;
    }

    public String getAutoApprovalCriteria() {
        return autoApprovalCriteria;
    }

    public void setAutoApprovalCriteria(String autoApprovalCriteria) {
        this.autoApprovalCriteria = autoApprovalCriteria;
    }

    // 修复 isShared 字段的 getter/setter 方法
    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    // 修复 isEligibleForPromise 字段的 getter/setter 方法
    public Boolean getEligibleForPromise() {
        return isEligibleForPromise;
    }

    public void setEligibleForPromise(Boolean eligibleForPromise) {
        isEligibleForPromise = eligibleForPromise;
    }

    public MaterialSource getMaterialSource() {
        return materialSource;
    }

    public void setMaterialSource(MaterialSource materialSource) {
        this.materialSource = materialSource;
    }

    public ProcessingMethodAndInfoAccess getProcessingMethodAndInfoAccess() {
        return processingMethodAndInfoAccess;
    }

    public void setProcessingMethodAndInfoAccess(ProcessingMethodAndInfoAccess processingMethodAndInfoAccess) {
        this.processingMethodAndInfoAccess = processingMethodAndInfoAccess;
    }

    @Override
    public String toString() {
        return "Material{ " +
                super.toString() +  // 包含父类的所有字段
                ", materialDetail='" + materialDetail + '\'' +
                ", reviewPoint='" + reviewPoint + '\'' +
                ", autoApprovalCriteria='" + autoApprovalCriteria + '\'' +
                ", isShared=" + isShared +
                ", materialSource=" + materialSource +
                ", processingMethodAndInfoAccess=" + processingMethodAndInfoAccess +
                ", isEligibleForPromise=" + isEligibleForPromise +
                '}';
    }


    public enum MaterialSource {
        PERSONAL_SUBMISSION("申请人自备"),
        SYSTEM_AUTO_SHARED("系统自动获取"),
        WANG_SHAN_PROCESSING("网上办理");

        private final String description;

        MaterialSource(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public enum ProcessingMethodAndInfoAccess {
        ONLINE_PROCESSING("网上办理，线上提交材料"),
        SYSTEM_AUTO_WITH_FALLBACK("系统自动获取，如数据不全则需申请者提交"),
        PAPER_CERTIFICATE("纸质证书需申请者提交");

        private final String description;

        ProcessingMethodAndInfoAccess(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
