package com.boylegu.springboot_vue.validation;

import com.boylegu.springboot_vue.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

@Component
public class MaterialIdsExistValidator implements ConstraintValidator<MaterialIdsExist, List<Long>> {
    
    @Autowired
    private MaterialRepository materialRepository;
    
    private static MaterialRepository staticMaterialRepository;
    
    @PostConstruct
    public void init() {
        staticMaterialRepository = materialRepository;
    }
    
    @Override
    public boolean isValid(List<Long> materialIds, ConstraintValidatorContext context) {
        if (materialIds == null || materialIds.isEmpty()) {
            return true; // 空列表认为是有效的
        }
        
        // 检查每个ID是否存在
        for (Long materialId : materialIds) {
            if (materialId != null && !staticMaterialRepository.existsById(materialId)) {
                return false; // 如果任何一个ID不存在，返回false
            }
        }
        
        return true;
    }
}
