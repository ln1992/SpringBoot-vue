package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Matter;

import java.util.List;

public interface MatterService {

    List<Matter> getAllMatters();

    Matter getMatterById(Long id);

    Matter saveMatter(Matter matter);

    Matter updateMatter(Long id, Matter matterDetails);

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
    List<Matter> getMattersByApprovalLevel(String approvalLevel);

    /**
     * 根据省厅对口指导处室（单位）查询事项列表
     * @param provincialDepartmentOffice 省厅对口指导处室（单位）
     * @return 事项列表
     */
    List<Matter> getMattersByProvincialDepartmentOffice(String provincialDepartmentOffice);

    /**
     * 激活事项（设置为有效）
     * @param id 事项ID
     * @return 更新后的事项对象，如果未找到则返回null
     */
    Matter activateMatter(Long id);

    /**
     * 停用事项（设置为无效）
     * @param id 事项ID
     * @return 更新后的事项对象，如果未找到则返回null
     */
    Matter deactivateMatter(Long id);

    /**
     * 发布事项
     * @param id 事项ID
     * @return 更新后的事项对象，如果未找到则返回null
     */
    Matter publishMatter(Long id);

    /**
     * 取消发布事项
     * @param id 事项ID
     * @return 更新后的事项对象，如果未找到则返回null
     */
    Matter unpublishMatter(Long id);

    /**
     * 根据版本号获取所有有效事项
     * @param version 版本号
     * @return 有效事项列表
     */
    List<Matter> findMattersByVersionAndValid(Long version);

    /**
     * 获取数据库中所有唯一的事项版本号
     * @return 版本号列表
     */
    List<Long> findAllVersions();

    /**
     * 获取数据库中所有最大的事项版本号
     * @return 版本号
     */
    Long findMaxVersion();

}