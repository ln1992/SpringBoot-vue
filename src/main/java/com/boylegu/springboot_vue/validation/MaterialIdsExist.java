package com.boylegu.springboot_vue.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaterialIdsExistValidator.class)
public @interface MaterialIdsExist {
    String message() default "一个或多个材料ID不存在";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
