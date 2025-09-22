// src/main/java/com/boylegu/springboot_vue/service/impl/MaterialServiceImpl.java
package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.aop.annotation.RecordUpdate;
import com.douyu.springboot_vue.entities.Material;
import com.douyu.springboot_vue.repository.MaterialRepository;
import com.douyu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Autowired
    public MaterialServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建材料")
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
        if (material.getValid() == null) {
            material.setValid(true);
        }

        // 更新名称
        material.updateName();

        return materialRepository.save(material);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "更新材料")
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
        existingMaterial.setValid(materialDetails.getValid());

        // 处理版本号
        if (materialDetails.getVersion() != null) {
            existingMaterial.setVersion(materialDetails.getVersion());
        }

        // 更新名称
        existingMaterial.updateName();

        return materialRepository.save(existingMaterial);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除材料")
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "激活材料")
    public Material activateMaterial(Long id) {
        Material material = getMaterialById(id);
        if (material != null) {
            material.setValid(true);
            return materialRepository.save(material);
        }
        return null;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "停用材料")
    public Material deactivateMaterial(Long id) {
        Material material = getMaterialById(id);
        if (material != null) {
            material.setValid(false);
            return materialRepository.save(material);
        }
        return null;
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

    @Override
    public Map<Long, Material> getMaterialsMapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        List<Material> materials = materialRepository.findAllById(ids);
        return materials.stream()
                .collect(Collectors.toMap(Material::getId, material -> material));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllMaterialVersions() {
        return materialRepository.findAllVersions();
    }
}