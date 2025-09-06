// src/main/java/com/boylegu/springboot_vue/service/MaterialService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Material;
import java.util.List;

public interface MaterialService {

    List<Material> getAllMaterials();

    Material getMaterialById(Long id);

    Material saveMaterial(Material material) throws IllegalArgumentException;

    Material updateMaterial(Long id, Material materialDetails) throws IllegalArgumentException;

    void deleteMaterial(Long id);

    // 上线材料
    boolean activateMaterial(Long id);

    // 下线材料
    boolean deactivateMaterial(Long id);

    // 根据有效性获取材料
    List<Material> getMaterialsByIsValid(Boolean isValid);

    // 检查材料明细和版本号组合是否存在
    boolean isMaterialDetailAndVersionExists(String materialDetail, Long version, Long excludeId);
}
