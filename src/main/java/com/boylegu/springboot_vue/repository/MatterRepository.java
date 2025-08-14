package com.boylegu.springboot_vue.repository;

import com.boylegu.springboot_vue.entities.Matter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Matter实体的Repository接口
 */
@Repository
public interface MatterRepository extends JpaRepository<Matter, Long> {
    
    /**
     * 根据主项名称查询事项列表
     * @param mainItemName 主项名称
     * @return 事项列表
     */
    List<Matter> findByMainItemName(String mainItemName);
    
    /**
     * 根据审批层级查询事项列表
     * @param approvalLevel 审批层级
     * @return 事项列表
     */
    List<Matter> findByApprovalLevel(Matter.ApprovalLevel approvalLevel);
    
    /**
     * 根据省厅对口指导处室（单位）查询事项列表
     * @param provincialDepartmentOffice 省厅对口指导处室（单位）
     * @return 事项列表
     */
    List<Matter> findByProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice provincialDepartmentOffice);
    
    /**
     * 根据主项名称和子项名称查询事项
     * @param mainItemName 主项名称
     * @param subItemName 子项名称
     * @return 事项列表
     */
    List<Matter> findByMainItemNameAndSubItemName(String mainItemName, String subItemName);

    Matter findByGrandchildItemName(String grandchildItemName);
}
