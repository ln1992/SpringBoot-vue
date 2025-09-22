// src/main/java/com/boylegu/springboot_vue/entities/Matter.java
package com.douyu.springboot_vue.entities;

import org.apache.commons.lang3.StringUtils;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 事项类 - 包含主项、子项、孙项及相关信息
 */
@Entity
@Table(name = "matter")
public class Matter extends BaseEntity {
    // ID 字段继承自 BaseEntity

    // 主项编号 - 必填
    @Column(name = "main_item_code")
    @NotNull(message = "主项编号不能为空")
    private Long mainItemCode;

    // 子项编号 - 可选
    @Column(name = "sub_item_code")
    private Long subItemCode;

    // 孙项编号 - 可选
    @Column(name = "grandchild_item_code")
    private Long grandchildItemCode;

    // 主项名称 - 必填
    @Column(name = "main_item_name")
    @NotBlank(message = "主项名称不能为空")
    private String mainItemName;

    // 子项名称 - 可选
    @Column(name = "sub_item_name")
    private String subItemName;

    // 孙项名称 - 可选
    @Column(name = "grandchild_item_name")
    private String grandchildItemName;

    // 经办依据list
    @ElementCollection
    @Column(name = "bases")
    private List<String> bases;

    // 材料 ID 列表
    @ElementCollection
    @Column(name = "material_id")
    private List<Long> materialIds;

    // 法定时限
    @Column(name = "legal_time_limit")
    @NotNull(message = "法定时限不能为空")
    private Long legalTimeLimit;

    // 承诺时限
    @Column(name = "committed_time_limit")
    @NotNull(message = "承诺时限不能为空")
    private Long committedTimeLimit;

    // 审批层级
    @Column(name = "approval_level")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "审批层级不能为空")
    private ApprovalLevel approvalLevel;

    // 省厅对口指导处室（单位）
    @Column(name = "provincial_department_office")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "省厅对口指导处室不能为空")
    private ProvincialDepartmentOffice provincialDepartmentOffice;

    //审批流程图
    @Column(name = "approval_process_diagram_id")
    private Long approvalProcessDiagramId;

    //业务经办流程图
    @Column(name = "business_process_diagram_id")
    private Long businessProcessDiagramId;

    // 是否发布（默认为false）
    @Column(name = "is_publish")
    private Boolean isPublish = false;

    // 默认构造函数
    public Matter() {}

    /**
     * 创建Matter比较器，按主项、子项、孙项编码排序
     */
    public static java.util.Comparator<Matter> createCodeComparator() {
        return java.util.Comparator
                .comparing(Matter::getMainItemCode, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()))
                .thenComparing(Matter::getSubItemCode, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()))
                .thenComparing(Matter::getGrandchildItemCode, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()));
    }
    public String getMainItemCodeName() {
        if (this.mainItemCode != null && StringUtils.isNotBlank(this.mainItemName)) {
            return String.format("%d.%s", this.mainItemCode, this.mainItemName);
        }
        return "";
    }

    public String getSubItemCodeName() {
        if (this.mainItemCode != null && this.subItemCode != null && StringUtils.isNotBlank(this.subItemName)) {
            return String.format("%d.%d %s", this.mainItemCode, this.subItemCode, this.subItemName);
        }
        return "";
    }

    public String getGrandchildItemCodeName() {
        if (this.mainItemCode != null && this.subItemCode != null &&
                this.grandchildItemCode != null && StringUtils.isNotEmpty(this.grandchildItemName)) {
            return String.format("%d.%d.%d %s", this.mainItemCode, this.subItemCode, this.grandchildItemCode, this.grandchildItemName);
        }
        return "";
    }

    /**
     * 更新事项名称
     * 优先级：孙项名称 > 子项名称 > 主项名称
     */
    public void updateName() {
        String name = "default_name";
        // 构建名称，优先使用孙项名称，然后是子项名称，最后是主项名称
        if (this.grandchildItemCode != null && StringUtils.isNotEmpty(this.grandchildItemName)) {
            // 如果有孙项名称，只使用孙项名称
            name = String.format("%d.%d.%d.%s", this.mainItemCode, this.subItemCode, this.grandchildItemCode, this.grandchildItemName);
        } else if (this.subItemCode != null && StringUtils.isNotEmpty(this.subItemName)) {
            // 如果没有孙项名称但有子项名称，只使用子项名称
            name = String.format("%d.%d.%s", this.mainItemCode, this.subItemCode, this.subItemName);
        } else if (this.mainItemCode != null && StringUtils.isNotEmpty(this.mainItemName)) {
            // 如果只有主项名称，使用主项名称
            name = String.format("%d.%s", this.mainItemCode, this.mainItemName);
        }
        // 如果有版本号，则在名称后加上版本号
        if (this.getVersion() != null) {
            name = String.format("%s_v%d", name, this.getVersion());
        }
        // 设置名称
        this.set__name__(name);
    }

    @Override
    public void setVersion(Long version) {
        super.setVersion(version);
        // 版本号更新时自动更新名称
        updateName();
    }

    // Getter和Setter方法 - ID 相关方法继承自 BaseEntity

    public Long getMainItemCode() {
        return mainItemCode;
    }

    public void setMainItemCode(Long mainItemCode) {
        this.mainItemCode = mainItemCode;
        updateName();
    }

    public Long getSubItemCode() {
        return subItemCode;
    }

    public void setSubItemCode(Long subItemCode) {
        this.subItemCode = subItemCode;
        updateName();
    }

    public Long getGrandchildItemCode() {
        return grandchildItemCode;
    }

    public void setGrandchildItemCode(Long grandchildItemCode) {
        this.grandchildItemCode = grandchildItemCode;
        updateName();
    }

    public String getMainItemName() {
        return mainItemName;
    }

    public void setMainItemName(String mainItemName) {
        this.mainItemName = mainItemName;
        updateName();
    }

    public String getSubItemName() {
        return subItemName;
    }

    public void setSubItemName(String subItemName) {
        this.subItemName = subItemName;
        updateName();
    }

    public String getGrandchildItemName() {
        return grandchildItemName;
    }

    public void setGrandchildItemName(String grandchildItemName) {
        this.grandchildItemName = grandchildItemName;
        updateName();
    }

    public List<String> getBases() {
        return bases;
    }

    public void setBases(List<String> basisList) {
        this.bases = basisList;
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

    public Long getApprovalProcessDiagramId() {
        return approvalProcessDiagramId;
    }

    public void setApprovalProcessDiagramId(Long approvalProcessDiagramId) {
        this.approvalProcessDiagramId = approvalProcessDiagramId;
    }

    public Long getBusinessProcessDiagramId() {
        return businessProcessDiagramId;
    }

    public void setBusinessProcessDiagramId(Long businessProcessDiagramId) {
        this.businessProcessDiagramId = businessProcessDiagramId;
    }

    public Boolean getPublish() {
        return isPublish;
    }

    public void setPublish(Boolean publish) {
        this.isPublish = publish;
    }

    @Override
    public String toString() {
        return "Matter{ " +
                super.toString() +  // 直接包含父类的所有字段
                ", mainItemCode=" + mainItemCode +
                ", subItemCode=" + subItemCode +
                ", grandchildItemCode=" + grandchildItemCode +
                ", mainItemName='" + mainItemName + '\'' +
                ", subItemName='" + subItemName + '\'' +
                ", grandchildItemName='" + grandchildItemName + '\'' +
                ", bases=" + bases +
                ", materialIds=" + materialIds +
                ", legalTimeLimit=" + legalTimeLimit +
                ", committedTimeLimit=" + committedTimeLimit +
                ", approvalLevel=" + approvalLevel +
                ", provincialDepartmentOffice=" + provincialDepartmentOffice +
                ", approvalProcessDiagramId=" + approvalProcessDiagramId +
                ", businessProcessDiagramId=" + businessProcessDiagramId +
                ", isPublish=" + isPublish +
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