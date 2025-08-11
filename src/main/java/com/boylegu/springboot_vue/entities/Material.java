package com.boylegu.springboot_vue.entities;

import javax.persistence.*;

/**
 * 材料类 - 用于描述各种申请或办理材料的信息
 */
@Entity
@Table(name = "material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 材料明细
    @Column(name = "material_details", unique = true)
    private String materialDetails;

    // 审核要点
    @Column(name = "review_points")
    private String reviewPoints;

    // "智能秒批"判断标准
    @Column(name = "auto_approval_criteria")
    private String autoApprovalCriteria;

    // 是否共享
    @Column(name = "is_shared")
    private Boolean isShared;

    // 材料来源
    @Column(name = "material_source")
    @Enumerated(EnumType.STRING)
    private MaterialSource materialSource;

    // 办理方式及材料信息获取方式说明
    @Column(name = "processing_method_and_info_access")
    private String processingMethodAndInfoAccess;

    // 是否适用告知承诺
    @Column(name = "is_eligible_for_promise")
    private Boolean isEligibleForPromise;

    // 是否有效（默认为true）
    @Column(name = "is_valid")
    private Boolean isValid = true;

    // 默认构造函数
    public Material() {}

    // 完整参数构造函数
    public Material(String materialDetails, String reviewPoints, String autoApprovalCriteria,
                    Boolean isShared, MaterialSource materialSource, String processingMethodAndInfoAccess,
                    Boolean isEligibleForPromise) {
        this.materialDetails = materialDetails;
        this.reviewPoints = reviewPoints;
        this.autoApprovalCriteria = autoApprovalCriteria;
        this.isShared = isShared;
        this.materialSource = materialSource;
        this.processingMethodAndInfoAccess = processingMethodAndInfoAccess;
        this.isEligibleForPromise = isEligibleForPromise;
        this.isValid = true; // 设置默认值为true
    }

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialDetails() {
        return materialDetails;
    }

    public void setMaterialDetails(String materialDetails) {
        this.materialDetails = materialDetails;
    }

    public String getReviewPoints() {
        return reviewPoints;
    }

    public void setReviewPoints(String reviewPoints) {
        this.reviewPoints = reviewPoints;
    }

    public String getAutoApprovalCriteria() {
        return autoApprovalCriteria;
    }

    public void setAutoApprovalCriteria(String autoApprovalCriteria) {
        this.autoApprovalCriteria = autoApprovalCriteria;
    }

    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    public MaterialSource getMaterialSource() {
        return materialSource;
    }

    public void setMaterialSource(MaterialSource materialSource) {
        this.materialSource = materialSource;
    }

    public String getProcessingMethodAndInfoAccess() {
        return processingMethodAndInfoAccess;
    }

    public void setProcessingMethodAndInfoAccess(String processingMethodAndInfoAccess) {
        this.processingMethodAndInfoAccess = processingMethodAndInfoAccess;
    }

    public Boolean getEligibleForPromise() {
        return isEligibleForPromise;
    }

    public void setEligibleForPromise(Boolean eligibleForPromise) {
        isEligibleForPromise = eligibleForPromise;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", materialDetails='" + materialDetails + '\'' +
                ", reviewPoints='" + reviewPoints + '\'' +
                ", autoApprovalCriteria='" + autoApprovalCriteria + '\'' +
                ", isShared=" + isShared +
                ", materialSource=" + materialSource +
                ", processingMethodAndInfoAccess='" + processingMethodAndInfoAccess + '\'' +
                ", isEligibleForPromise=" + isEligibleForPromise +
                ", isValid=" + isValid +
                '}';
    }

    public enum MaterialSource {
        PERSONAL_SUBMISSION,     // 个人提交网上办理，线上提交材料
        SYSTEM_AUTO_SHARED    // 系统自动获取，如数据不全则需申请者提交
    }
}
