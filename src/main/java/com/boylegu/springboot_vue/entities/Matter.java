// src/main/java/com/boylegu/springboot_vue/entities/Matter.java
package com.boylegu.springboot_vue.entities;

import com.boylegu.springboot_vue.validation.MaterialIdsExist;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 事项类 - 包含主项、子项、孙项及相关信息
 */
@Entity
@Table(name = "matter")
public class Matter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 主项编号
    @Column(name = "main_item_code")
    private Long mainItemCode;

    // 子项编号
    @Column(name = "sub_item_code")
    private Long subItemCode;

    // 孙项编号
    @Column(name = "grandchild_item_code")
    private Long grandchildItemCode;

    // 主项名称
    @Column(name = "main_item_name")
    private String mainItemName;

    // 子项名称
    @Column(name = "sub_item_name")
    private String subItemName;

    // 孙项名称
    @Column(name = "grandchild_item_name")
    private String grandchildItemName;

    // 经办依据list
    @ElementCollection
    @CollectionTable(name = "matter_basis", joinColumns = @JoinColumn(name = "matter_id"))
    @Column(name = "basis")
    private List<String> basisList;

    // 材料 ID 列表
    @ElementCollection
    @CollectionTable(name = "matter_materials", joinColumns = @JoinColumn(name = "matter_id"))
    @Column(name = "material_id")
    //@MaterialIdsExist
    private List<Long> materialIds;

    // 法定时限
    @Column(name = "legal_time_limit")
    private Long legalTimeLimit;

    // 承诺时限
    @Column(name = "committed_time_limit")
    private Long committedTimeLimit;

    // 审批层级
    @Column(name = "approval_level")
    @Enumerated(EnumType.STRING)
    private ApprovalLevel approvalLevel;

    // 省厅对口指导处室（单位）
    @Column(name = "provincial_department_office")
    @Enumerated(EnumType.STRING)
    private ProvincialDepartmentOffice provincialDepartmentOffice;

    // 是否有效（默认为true）
    @Column(name = "is_valid")
    private Boolean isValid = true;

    // 默认构造函数
    public Matter() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getMainItemName() {
        return mainItemName;
    }

    public void setMainItemName(String mainItemName) {
        this.mainItemName = mainItemName;
    }

    public String getSubItemName() {
        return subItemName;
    }

    public void setSubItemName(String subItemName) {
        this.subItemName = subItemName;
    }

    public String getGrandchildItemName() {
        return grandchildItemName;
    }

    public void setGrandchildItemName(String grandchildItemName) {
        this.grandchildItemName = grandchildItemName;
    }

    public List<String> getBasisList() {
        return basisList;
    }

    public void setBasisList(List<String> basisList) {
        this.basisList = basisList;
    }

    public List<Long> getMaterialIds() {
        return materialIds;
    }

    public void setMaterialIds(List<Long> materialIds) {
        this.materialIds = materialIds;
    }

    public Long getLegalTimeLimit() {
        return legalTimeLimit;
    }

    public void setLegalTimeLimit(Long legalTimeLimit) {
        this.legalTimeLimit = legalTimeLimit;
    }

    public Long getCommittedTimeLimit() {
        return committedTimeLimit;
    }

    public void setCommittedTimeLimit(Long committedTimeLimit) {
        this.committedTimeLimit = committedTimeLimit;
    }

    public ApprovalLevel getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(ApprovalLevel approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public ProvincialDepartmentOffice getProvincialDepartmentOffice() {
        return provincialDepartmentOffice;
    }

    public void setProvincialDepartmentOffice(ProvincialDepartmentOffice provincialDepartmentOffice) {
        this.provincialDepartmentOffice = provincialDepartmentOffice;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "Matter{" +
                "id=" + id +
                ", mainItemCode=" + mainItemCode + '\'' +
                ", subItemCode=" + subItemCode + '\'' +
                ", grandchildItemCode=" + grandchildItemCode + '\'' +
                ", mainItemName='" + mainItemName + '\'' +
                ", subItemName='" + subItemName + '\'' +
                ", grandchildItemName='" + grandchildItemName + '\'' +
                ", basisList=" + basisList +
                ", materialIds=" + materialIds +
                ", legalTimeLimit='" + legalTimeLimit + '\'' +
                ", committedTimeLimit='" + committedTimeLimit + '\'' +
                ", approvalLevel='" + approvalLevel + '\'' +
                ", provincialDepartmentOffice='" + provincialDepartmentOffice + '\'' +
                ", isValid=" + isValid +
                '}';
    }

    /**
     * 审批层级枚举
     */
    public enum ApprovalLevel {
        PROVINCIAL("省级"),
        PROVINCIAL_MUNICIPAL("省市两级"),
        PROVINCIAL_MUNICIPAL_COUNTY("省市县三级"),
        MUNICIPAL("设区的市"),
        MUNICIPAL_COUNTY("市县两级"),
        COUNTY("县级");

        private final String description;

        ApprovalLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * 省厅对口指导处室（单位）枚举
     */
    public enum ProvincialDepartmentOffice {
        // 厅的处室
        PROVINCIAL_DEPARTMENT_POLICY_REGULATIONS("厅政策法规处"),
        PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL("厅行政审批处"),
        PROVINCIAL_DEPARTMENT_TRANSPORTATION_MANAGEMENT("厅运输管理处"),

        // 省港航中心的处室
        PROVINCIAL_PORT_CENTER_CONSTRUCTION("省港航中心建设处"),
        PROVINCIAL_PORT_CENTER_MANAGEMENT("省港航中心管理处"),

        // 省公路中心的处室
        PROVINCIAL_HIGHWAY_CENTER_CONSTRUCTION("省公路中心建设处"),
        PROVINCIAL_HIGHWAY_CENTER_MAINTENANCE("省公路中心养护处");

        private final String description;

        ProvincialDepartmentOffice(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
