// src/main/java/com/boylegu/springboot_vue/service/MatterService.java

package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.repository.MatterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@DependsOn("materialService")
public class MatterService {

    @Autowired
    private MatterRepository matterRepository;

    @PostConstruct
    public void init() {
        // 检查是否已有数据，如果没有则添加测试数据
        if (matterRepository.count() == 0) {
            addSampleData();
        }
    }

    private void addSampleData() {
        Matter matter1 = new Matter();
        matter1.setMainItemName("交通行政许可");
        matter1.setSubItemName("道路运输经营许可");
        matter1.setGrandchildItemName("客运经营许可");
        //matter1.setBasisList(List.of("《中华人民共和国道路运输条例》", "《道路旅客运输及客运站管理规定》"));
        //matter1.setMaterialIds(List.of(1L, 2L));
        matter1.setLegalTimeLimit(20L);
        matter1.setCommittedTimeLimit(10L);
        matter1.setApprovalLevel(Matter.ApprovalLevel.PROVINCIAL_MUNICIPAL);
        matter1.setProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice.PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL);

        Matter matter2 = new Matter();
        matter2.setMainItemName("交通行政许可");
        matter2.setSubItemName("道路运输经营许可");
        matter2.setGrandchildItemName("货运经营许可");
        //matter2.setBasisList(List.of("《中华人民共和国道路运输条例》", "《道路货物运输及站场管理规定》"));
        //matter2.setMaterialIds(List.of(1L));
        matter2.setLegalTimeLimit(15L);
        matter2.setCommittedTimeLimit(7L);
        matter2.setApprovalLevel(Matter.ApprovalLevel.MUNICIPAL);
        matter2.setProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice.PROVINCIAL_PORT_CENTER_CONSTRUCTION);

        matterRepository.save(matter1);
        matterRepository.save(matter2);
    }

    public List<Matter> getAllMatters() {
        return matterRepository.findAll();
    }

    public Matter getMatterById(Long id) {
        return matterRepository.findById(id).orElse(null);
    }

    public Matter saveMatter(Matter matter) {
        return matterRepository.save(matter);
    }

    public void deleteMatter(Long id) {
        matterRepository.deleteById(id);
    }

    /**
     * 根据主项名称查询事项列表
     * @param mainItemName 主项名称
     * @return 事项列表
     */
    public List<Matter> getMattersByMainItemName(String mainItemName) {
        return matterRepository.findByMainItemName(mainItemName);
    }

    /**
     * 根据审批层级查询事项列表
     * @param approvalLevel 审批层级
     * @return 事项列表
     */
    public List<Matter> getMattersByApprovalLevel(Matter.ApprovalLevel approvalLevel) {
        return matterRepository.findByApprovalLevel(approvalLevel);
    }

    /**
     * 根据省厅对口指导处室（单位）查询事项列表
     * @param provincialDepartmentOffice 省厅对口指导处室（单位）
     * @return 事项列表
     */
    public List<Matter> getMattersByProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice provincialDepartmentOffice) {
        return matterRepository.findByProvincialDepartmentOffice(provincialDepartmentOffice);
    }

    // 新增：激活事项（上线）
    public Matter activateMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setIsValid(true);
            return matterRepository.save(matter);
        }
        return null;
    }

    // 新增：停用事项（下线）
    public Matter deactivateMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setIsValid(false);
            return matterRepository.save(matter);
        }
        return null;
    }
}
