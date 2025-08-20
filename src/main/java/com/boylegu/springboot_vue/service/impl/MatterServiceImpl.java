// src/main/java/com/boylegu/springboot_vue/service/impl/MatterServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.repository.MatterRepository;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatterServiceImpl implements MatterService {

    @Autowired
    private MatterRepository matterRepository;

    @Override
    public List<Matter> getAllMatters() {
        return matterRepository.findAll();
    }

    @Override
    public Matter getMatterById(Long id) {
        return matterRepository.findById(id).orElse(null);
    }

    @Override
    public Matter saveMatter(Matter matter) {
        // 创建新的ArrayList避免ConcurrentModificationException
        if (matter.getBasisList() != null) {
            matter.setBasisList(new ArrayList<>(matter.getBasisList()));
        }
        if (matter.getMaterialIds() != null) {
            matter.setMaterialIds(new ArrayList<>(matter.getMaterialIds()));
        }

        return matterRepository.save(matter);
    }

    @Override
    public void deleteMatter(Long id) {
        matterRepository.deleteById(id);
    }

    @Override
    public List<Matter> getMattersByMainItemName(String mainItemName) {
        return matterRepository.findByMainItemName(mainItemName);
    }

    @Override
    public List<Matter> getMattersByApprovalLevel(Matter.ApprovalLevel approvalLevel) {
        return matterRepository.findByApprovalLevel(approvalLevel);
    }

    @Override
    public List<Matter> getMattersByProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice provincialDepartmentOffice) {
        return matterRepository.findByProvincialDepartmentOffice(provincialDepartmentOffice);
    }

    @Override
    public Matter activateMatter(Long id) {
        Optional<Matter> matterOptional = matterRepository.findById(id);
        if (matterOptional.isPresent()) {
            Matter matter = matterOptional.get();
            matter.setIsValid(true);
            return matterRepository.save(matter);
        }
        return null;
    }

    @Override
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
