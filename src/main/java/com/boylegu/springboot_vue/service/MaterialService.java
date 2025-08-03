package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {
    
    @Autowired
    private MaterialRepository materialRepository;
    
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
}
