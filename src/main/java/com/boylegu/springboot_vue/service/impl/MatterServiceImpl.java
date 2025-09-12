// src/main/java/com/boylegu/springboot_vue/service/impl/MatterServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.repository.MatterRepository;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class MatterServiceImpl implements MatterService {

    private static final Logger logger = Logger.getLogger(MatterServiceImpl.class.getName());

    @Autowired
    private MatterRepository matterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Matter> getAllMatters() {
        return matterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Matter getMatterById(Long id) {
        return matterRepository.findById(id).orElse(null);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建事项")
    public Matter saveMatter(Matter matter) {
        // 创建新的ArrayList避免ConcurrentModificationException
        if (matter.getBases() != null) {
            matter.setBases(new ArrayList<>(matter.getBases()));
        }
        if (matter.getMaterialIds() != null) {
            matter.setMaterialIds(new ArrayList<>(matter.getMaterialIds()));
        }

        return matterRepository.save(matter);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "更新事项")
    public Matter updateMatter(Long id, Matter matterDetails) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();

            // 更新字段
            matter.setMainItemCode(matterDetails.getMainItemCode());
            matter.setSubItemCode(matterDetails.getSubItemCode());
            matter.setGrandchildItemCode(matterDetails.getGrandchildItemCode());
            matter.setMainItemName(matterDetails.getMainItemName());
            matter.setSubItemName(matterDetails.getSubItemName());
            matter.setGrandchildItemName(matterDetails.getGrandchildItemName());
            matter.setBases(matterDetails.getBases());
            matter.setMaterialIds(matterDetails.getMaterialIds());
            matter.setLegalTimeLimit(matterDetails.getLegalTimeLimit());
            matter.setCommittedTimeLimit(matterDetails.getCommittedTimeLimit());
            matter.setApprovalLevel(matterDetails.getApprovalLevel());
            matter.setProvincialDepartmentOffice(matterDetails.getProvincialDepartmentOffice());
            matter.setApprovalProcessDiagramId(matterDetails.getApprovalProcessDiagramId());
            matter.setBusinessProcessDiagramId(matterDetails.getBusinessProcessDiagramId());
            matter.setVersion(matterDetails.getVersion());
            matter.setPublish(matterDetails.getPublish());
            matter.setValid(matterDetails.getValid());
            
            // 确保更新名称
            matter.updateName();

            return matterRepository.save(matter);
        }
        return null;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除事项")
    public void deleteMatter(Long id) {
        matterRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Matter> getMattersByMainItemName(String mainItemName) {
        return matterRepository.findByMainItemName(mainItemName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Matter> getMattersByApprovalLevel(String approvalLevel) {
        try {
            Matter.ApprovalLevel level = Matter.ApprovalLevel.valueOf(approvalLevel);
            return matterRepository.findByApprovalLevel(level);
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid approval level: " + approvalLevel);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Matter> getMattersByProvincialDepartmentOffice(String provincialDepartmentOffice) {
        try {
            Matter.ProvincialDepartmentOffice office = Matter.ProvincialDepartmentOffice.valueOf(provincialDepartmentOffice);
            return matterRepository.findByProvincialDepartmentOffice(office);
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid provincial department office: " + provincialDepartmentOffice);
            throw e;
        }
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "激活事项")
    public Matter activateMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setValid(true);
            return matterRepository.save(matter);
        }
        return null;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "停用事项")
    public Matter deactivateMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setValid(false);
            return matterRepository.save(matter);
        }
        return null;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "发布事项")
    public Matter publishMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setPublish(true);
            return matterRepository.save(matter);
        }
        return null;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "取消发布事项")
    public Matter unpublishMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setPublish(false);
            return matterRepository.save(matter);
        }
        return null;
    }
    
    /**
     * 根据版本号获取所有事项
     * @param version 版本号
     * @return 事项列表
     */
    @Override
    public List<Matter> getMattersByVersionAndValid(Long version) {
        return matterRepository.findByVersionAndIsValid(version, true);
    }

}