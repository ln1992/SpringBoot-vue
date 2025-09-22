// src/main/java/com/boylegu/springboot_vue/validation/MaterialIdsExistValidator.java
package com.douyu.springboot_vue.validation;

import com.douyu.springboot_vue.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

// 保持@Component注解，这样Spring可以管理这个bean
@Component
public class MaterialIdsExistValidator implements ConstraintValidator<MaterialIdsExist, List<Long>> {

    // 使用Spring的@Autowired注入
    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public boolean isValid(List<Long> materialIds, ConstraintValidatorContext context) {
        // 如果材料ID列表为空或null，认为是有效的
        if (materialIds == null || materialIds.isEmpty()) {
            return true;
        }

        try {
            // 检查每个ID是否存在
            for (Long materialId : materialIds) {
                // 跳过null值
                if (materialId != null) {
                    // 确保 materialRepository 不为 null
                    if (materialRepository == null) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(
                                        "验证器配置错误：无法注入 MaterialRepository")
                                .addConstraintViolation();
                        return false;
                    }

                    // 如果任何一个ID不存在，返回false
                    if (!materialRepository.existsById(materialId)) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(
                                        "材料ID " + materialId + " 不存在")
                                .addConstraintViolation();
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            // 发生异常时，禁用默认约束违规并添加自定义消息
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "验证材料ID时发生错误: " + e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}
