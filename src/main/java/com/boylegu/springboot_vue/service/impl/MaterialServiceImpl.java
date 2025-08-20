// src/main/java/com/boylegu/springboot_vue/service/impl/MaterialServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.Material.MaterialSource;
import com.boylegu.springboot_vue.repository.MaterialRepository;
import com.boylegu.springboot_vue.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

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
    public Material saveMaterial(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    @Override
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

    @Override
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
