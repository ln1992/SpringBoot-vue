// src/main/java/com/boylegu/springboot_vue/service/impl/MaterialServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.repository.MaterialRepository;
import com.boylegu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    @Override
    public Material saveMaterial(Material material) throws IllegalArgumentException {
        // 确保版本号不为null
        if (material.getVersion() == null) {
            material.setVersion(1L);
        }

        // 验证材料明细和版本号的组合是否唯一
        if (isMaterialDetailAndVersionExists(material.getMaterialDetail(), material.getVersion(), null)) {
            throw new IllegalArgumentException("材料明细和版本号的组合已存在");
        }

        // 设置默认值
        if (material.getIsValid() == null) {
            material.setIsValid(true);
        }

        // 更新名称
        material.updateName();

        return materialRepository.save(material);
    }

    @Override
    public Material updateMaterial(Long id, Material materialDetails) throws IllegalArgumentException {
        Material existingMaterial = getMaterialById(id);
        if (existingMaterial == null) {
            return null;
        }

        // 确保版本号不为null
        if (materialDetails.getVersion() == null) {
            materialDetails.setVersion(existingMaterial.getVersion());
        }

        // 验证材料明细和版本号的组合是否唯一（排除当前记录）
        if (isMaterialDetailAndVersionExists(materialDetails.getMaterialDetail(), materialDetails.getVersion(), id)) {
            throw new IllegalArgumentException("材料明细和版本号的组合已存在");
        }

        // 更新所有字段
        existingMaterial.setMaterialDetail(materialDetails.getMaterialDetail());
        existingMaterial.setReviewPoint(materialDetails.getReviewPoint());
        existingMaterial.setAutoApprovalCriteria(materialDetails.getAutoApprovalCriteria());
        existingMaterial.setShared(materialDetails.getShared()); // 修复字段名
        existingMaterial.setMaterialSource(materialDetails.getMaterialSource());
        existingMaterial.setProcessingMethodAndInfoAccess(materialDetails.getProcessingMethodAndInfoAccess());
        existingMaterial.setEligibleForPromise(materialDetails.getEligibleForPromise()); // 修复字段名
        existingMaterial.setIsValid(materialDetails.getIsValid());

        // 处理版本号
        if (materialDetails.getVersion() != null) {
            existingMaterial.setVersion(materialDetails.getVersion());
        }

        // 更新名称
        existingMaterial.updateName();

        return materialRepository.save(existingMaterial);
    }

    @Override
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    @Override
    public boolean activateMaterial(Long id) {
        Material material = getMaterialById(id);
        if (material != null) {
            material.setIsValid(true);
            materialRepository.save(material);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateMaterial(Long id) {
        Material material = getMaterialById(id);
        if (material != null) {
            material.setIsValid(false);
            materialRepository.save(material);
            return true;
        }
        return false;
    }

    @Override
    public List<Material> getMaterialsByIsValid(Boolean isValid) {
        return materialRepository.findByIsValid(isValid);
    }

    @Override
    public boolean isMaterialDetailAndVersionExists(String materialDetail, Long version, Long excludeId) {
        if (excludeId != null) {
            return materialRepository.countByMaterialDetailAndVersionExcludingId(materialDetail, version, excludeId) > 0;
        } else {
            return materialRepository.countByMaterialDetailAndVersion(materialDetail, version) > 0;
        }
    }
}
