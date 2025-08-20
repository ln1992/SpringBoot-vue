// src/main/java/com/boylegu/springboot_vue/service/MaterialService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.Material.MaterialSource;

import java.util.List;

public interface MaterialService {

    List<Material> getAllMaterials();

    Material getMaterialById(Long id);

    Material saveMaterial(Material material);

    void deleteMaterial(Long id);

    // 上线材料
    boolean activateMaterial(Long id);

    // 下线材料
    boolean deactivateMaterial(Long id);
}
