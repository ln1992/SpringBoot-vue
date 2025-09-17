// src/main/java/com/boylegu/springboot_vue/aop/UpdateRecordAspect.java
package com.boylegu.springboot_vue.aop;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.BaseEntity;
import com.boylegu.springboot_vue.service.UpdateRecordService;
import com.boylegu.springboot_vue.util.ObjectCompareUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * 更新记录切面类
 */
@Aspect
@Component
public class UpdateRecordAspect {

    private static final Logger logger = Logger.getLogger(UpdateRecordAspect.class.getName());

    @Autowired
    private UpdateRecordService updateRecordService;

    // 定义需要忽略的字段列表
    private static final Set<String> IGNORED_FIELDS = new HashSet<>(Arrays.asList(
            "updateTime"
    ));

    private static final Set<String> HASH_FIELDS = new HashSet<>(Arrays.asList(
            "imageData",
            "imageUrl"
    ));


    /**
     * 环围通知，处理带有 @RecordUpdate 注解的方法
     */
    @Around("@annotation(recordUpdate)")
    public Object recordUpdateOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        logger.info("开始记录操作: " + className + "." + methodName);

        // 获取方法参数
        Object[] args = joinPoint.getArgs();

        // 根据操作类型处理
        switch (recordUpdate.operation()) {
            case CREATE:
                return handleCreateOperation(joinPoint, recordUpdate, args);
            case UPDATE:
                return handleUpdateOperation(joinPoint, recordUpdate, args);
            case DELETE:
                return handleDeleteOperation(joinPoint, recordUpdate, args);
            default:
                return joinPoint.proceed();
        }
    }

    /**
     * 处理创建操作
     */
    private Object handleCreateOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        // 执行原方法
        Object result = joinPoint.proceed();

        // 如果返回结果是 BaseEntity 或其子类，则记录创建操作
        if (result instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) result;
            String operator = getCurrentUser();
            String description = buildDescription(recordUpdate, "创建");

            updateRecordService.logCreate(entity, operator, description);
        }

        return result;
    }

    /**
     * 处理更新操作
     */
    private Object handleUpdateOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        // 在执行方法前获取旧实体状态
        BaseEntity oldEntity = extractOldEntityFromArgs(joinPoint, args);

        // 如果成功获取到旧实体，创建其副本用于记录
        Map<String, Object> oldValues = null;
        if (oldEntity != null) {
            oldValues = extractEntityFields(oldEntity);
        }

        try {
            // 执行原方法
            Object result = joinPoint.proceed();

            // 如果返回结果是 BaseEntity 或其子类，则记录更新操作
            if (result instanceof BaseEntity) {
                BaseEntity newEntity = (BaseEntity) result;
                String operator = getCurrentUser();
                String description = buildDescription(recordUpdate, "更新");

                // 获取新实体字段值
                Map<String, Object> newValues = extractEntityFields(newEntity);

                // 比较并获取变更字段
                Map<String, Object> changedOldValues = new HashMap<>();
                Map<String, Object> changedNewValues = new HashMap<>();

                if (oldValues != null) {
                    for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                        String fieldName = entry.getKey();
                        Object newValue = entry.getValue();
                        Object oldValue = oldValues.get(fieldName);

                        // 比较新旧值，只记录发生变化的字段
                        if (!IGNORED_FIELDS.contains(fieldName) && !ObjectCompareUtils.objectEqual(oldValue, newValue)) {
                            if (HASH_FIELDS.contains(fieldName)) {
                                logger.info("检查字段变更: " + fieldName);
                                logger.info("旧值: " + oldValue + " (类型: " + (oldValue != null ? oldValue.getClass().getName() : "null") + ")");
                                logger.info("新值: " + newValue + " (类型: " + (newValue != null ? newValue.getClass().getName() : "null") + ")");

                                changedOldValues.put(fieldName, computeHash(oldValue) );
                                changedNewValues.put(fieldName, computeHash(newValue) );
                            } else {
                                changedOldValues.put(fieldName, oldValue);
                                changedNewValues.put(fieldName, newValue);
                            }
                        }
                    }
                } else {
                    // 如果无法获取旧实体，则记录所有新字段
                    changedNewValues.putAll(newValues);
                    description += " (无法获取更新前状态)";
                }

                // 只有当有字段变更时才记录
                if (!changedOldValues.isEmpty() || !changedNewValues.isEmpty()) {
                    updateRecordService.logUpdate(
                            newEntity.getClass().getSimpleName(),
                            newEntity.getId(),
                            newEntity.get__name__(),
                            changedOldValues,
                            changedNewValues,
                            operator,
                            description
                    );
                }
            }

            return result;
        } catch (Exception e) {
            logger.severe("执行更新操作时发生异常: " + e.getMessage());
            // 打印完整的堆栈跟踪以便调试
            e.printStackTrace();
            // 重新抛出异常，确保业务逻辑不会因为记录更新而受到影响
            throw e;
        }
    }

    /**
     * 处理删除操作
     */
    private Object handleDeleteOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        // 尝试获取要删除的实体
        BaseEntity entityToDelete = extractEntityToDelete(joinPoint, args);

        // 执行原方法
        Object result = joinPoint.proceed();

        // 如果能获取到要删除的实体，则记录删除操作
        if (entityToDelete != null) {
            String operator = getCurrentUser();
            String description = buildDescription(recordUpdate, "删除");
            updateRecordService.logDelete(entityToDelete, operator, description);
        }

        return result;
    }

    /**
     * 提取实体的所有字段值
     */
    private Map<String, Object> extractEntityFields(BaseEntity entity) {
        Map<String, Object> fieldValues = new HashMap<>();

        try {
            // 获取所有字段（包括父类字段）
            Class<?> clazz = entity.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    fieldValues.put(field.getName(), field.get(entity));
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            logger.warning("无法访问实体字段: " + e.getMessage());
        }

        return fieldValues;
    }

    /**
     * 根据类名确定要调用的方法名
     */
    private String determineMethodNameByClassName(String className) {
        if (className.contains("Material")) {
            return "getMaterialById";
        } else if (className.contains("Matter")) {
            return "getMatterById";
        } else if (className.contains("ApprovalProcessDiagram")) {
            return "getDiagramById";
        } else if (className.contains("BusinessProcessDiagram")) {
            return "getDiagramById";
        } else {
            // 对于其他类型，暂时返回null
            return null;
        }
    }

    /**
     * 从参数中提取旧实体状态
     */
    private BaseEntity extractOldEntityFromArgs(ProceedingJoinPoint joinPoint, Object[] args) {
        // 查找参数中是否有ID（通常为Long类型）
        Long id = null;

        // 获取ID参数
        for (Object arg : args) {
            if (arg instanceof Long) {
                id = (Long) arg;
                break;
            }
        }

        // 如果没有找到ID，直接返回null
        if (id == null) {
            return null;
        }

        // 尝试通过反射调用Service的get方法获取旧实体
        try {
            Object target = joinPoint.getTarget();
            String className = target.getClass().getSimpleName();

            // 根据类名确定要调用的方法
            String methodName = determineMethodNameByClassName(className);
            
            // 如果无法确定方法名，返回null
            if (methodName == null) {
                return null;
            }

            // 通过反射调用对应的方法获取旧实体
            Method method = target.getClass().getMethod(methodName, Long.class);
            Object result = method.invoke(target, id);

            if (result instanceof BaseEntity) {
                return (BaseEntity) result;
            }
        } catch (Exception e) {
            logger.warning("无法获取旧实体: " + e.getMessage());
            // 打印完整的堆栈跟踪以便调试
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从参数中提取要删除的实体
     */
    private BaseEntity extractEntityToDelete(ProceedingJoinPoint joinPoint, Object[] args) {
        // 查找参数中是否有 BaseEntity 或其子类
        for (Object arg : args) {
            if (arg instanceof BaseEntity) {
                return (BaseEntity) arg;
            }
        }

        // 如果参数中有ID（通常为Long类型），则尝试通过反射获取实体
        Object target = joinPoint.getTarget();
        for (Object arg : args) {
            if (arg instanceof Long) {
                Long id = (Long) arg;
                try {
                    // 获取目标对象的类名
                    String className = target.getClass().getSimpleName();
                    
                    // 根据类名确定要调用的方法
                    String methodName = determineMethodNameByClassName(className);
                    
                    // 如果无法确定方法名，返回null
                    if (methodName == null) {
                        return null;
                    }

                    // 通过反射调用对应的方法获取要删除的实体
                    Method method = target.getClass().getMethod(methodName, Long.class);
                    Object result = method.invoke(target, id);

                    if (result instanceof BaseEntity) {
                        return (BaseEntity) result;
                    }
                } catch (Exception e) {
                    logger.warning("无法获取要删除的实体: " + e.getMessage());
                    // 打印完整的堆栈跟踪以便调试
                    e.printStackTrace();
                }
                return null;
            }
        }

        return null;
    }

    /**
     * 构建操作描述
     */
    private String buildDescription(RecordUpdate recordUpdate, String defaultAction) {
        if (!recordUpdate.description().isEmpty()) {
            return recordUpdate.description();
        }

        String action = recordUpdate.operation().getDescription();
        if (action == null || action.isEmpty()) {
            action = defaultAction;
        }

        return action + "操作";
    }

    /**
     * 获取当前操作用户
     */
    private String getCurrentUser() {
        // 在没有Spring Security的情况下，暂时返回默认用户
        return "system";
    }

    private String computeHash(Object entity) {
        try {
            String input = Optional.ofNullable(entity).map(Object::toString).orElse("");
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.warning("计算哈希值时出错: " + e.getMessage());
            return "hash_error";
        }
    }
}