package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.Material.MaterialSource;
import com.boylegu.springboot_vue.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@DependsOn("materialRepository")
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @PostConstruct
    public void init() {
        // 检查是否已有数据，如果没有则添加测试数据
        if (materialRepository.count() == 0) {
            addSampleData();
        }
    }

    private void addSampleData() {
        Material material1 = new Material();
        material1.setMaterialDetails("身份证复印件");
        material1.setReviewPoints("检查身份证信息是否清晰");
        material1.setAutoApprovalCriteria("身份证信息完整且清晰");
        material1.setShared(true);
        material1.setMaterialSource(MaterialSource.PERSONAL_SUBMISSION);
        material1.setProcessingMethodAndInfoAccess("在线提交");
        material1.setEligibleForPromise(true);
        material1.setIsValid(true); // 设置默认状态为上线

        Material material2 = new Material();
        material2.setMaterialDetails("户口本复印件");
        material2.setReviewPoints("检查户口本信息是否完整");
        material2.setAutoApprovalCriteria("户口本信息完整");
        material2.setShared(false);
        material2.setMaterialSource(MaterialSource.SYSTEM_AUTO_SHARED);
        material2.setProcessingMethodAndInfoAccess("系统自动获取");
        material2.setEligibleForPromise(false);
        material2.setIsValid(true); // 设置默认状态为上线

        materialRepository.save(material1);
        materialRepository.save(material2);
    }

    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    public Material getMaterialById(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    public Material saveMaterial(Material material) {
        return materialRepository.save(material);
    }

    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    // 上线材料
    public boolean activateMaterial(Long id) {
        Optional<Material> materialOptional = materialRepository.findById(id);
        if (materialOptional.isPresent()) {
            Material material = materialOptional.get();
            material.setIsValid(true);
            materialRepository.save(material);
            return true;
        }
        return false;
    }

    // 下线材料
    public boolean deactivateMaterial(Long id) {
        Optional<Material> materialOptional = materialRepository.findById(id);
        if (materialOptional.isPresent()) {
            Material material = materialOptional.get();
            material.setIsValid(false);
            materialRepository.save(material);
            return true;
        }
        return false;
    }
}
