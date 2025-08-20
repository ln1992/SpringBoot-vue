// src/main/java/com/boylegu/springboot_vue/service/MatterService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Matter;

import java.util.List;

public interface MatterService {

    List<Matter> getAllMatters();

    Matter getMatterById(Long id);

    Matter saveMatter(Matter matter);

    void deleteMatter(Long id);

    /**
     * 根据主项名称查询事项列表
     * @param mainItemName 主项名称
     * @return 事项列表
     */
    List<Matter> getMattersByMainItemName(String mainItemName);

    /**
     * 根据审批层级查询事项列表
     * @param approvalLevel 审批层级
     * @return 事项列表
     */
    List<Matter> getMattersByApprovalLevel(Matter.ApprovalLevel approvalLevel);

    /**
     * 根据省厅对口指导处室（单位）查询事项列表
     * @param provincialDepartmentOffice 省厅对口指导处室（单位）
     * @return 事项列表
     */
    List<Matter> getMattersByProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice provincialDepartmentOffice);

    // 新增：激活事项（上线）
    Matter activateMatter(Long id);

    // 新增：停用事项（下线）
    Matter deactivateMatter(Long id);
}
