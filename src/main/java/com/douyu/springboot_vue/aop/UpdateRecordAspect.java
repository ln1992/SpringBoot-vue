// src/main/java/com/boylegu/springboot_vue/aop/UpdateRecordAspect.java
package com.douyu.springboot_vue.aop;

import com.douyu.springboot_vue.aop.annotation.RecordUpdate;
import com.douyu.springboot_vue.entities.BaseEntity;
import com.douyu.springboot_vue.service.UpdateRecordService;
import com.douyu.springboot_vue.util.ObjectCompareUtils;
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

    // 静态 ObjectMapper 实例，避免重复创建
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UpdateRecordService updateRecordService;

    // 定义需要哈希处理的字段列表
    private static final Set<String> HASH_FIELDS = new HashSet<>(Arrays.asList(
            "imageData",
            "imageUrl",
            "cachedImageDataUrl"
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
            case BATCH_COPY:
                return handleBatchCopyOperation(joinPoint, recordUpdate, args);
            case BATCH_PUBLISH:
                return handleBatchPublishOperation(joinPoint, recordUpdate, args);
            default:
                return joinPoint.proceed();
        }
    }

    /**
     * 处理创建操作
     */
    private Object handleCreateOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) result;
            String operator = getCurrentUser();
            String description = buildDescription(recordUpdate, "创建");
            Map<String, Object> processedFields = processEntityFieldsForLogging(entity);

            updateRecordService.logCreate(
                    entity.getClass().getSimpleName(),
                    entity.getId(),
                    entity.get__name__(),
                    operator,
                    description,
                    serializeToJsonStr(processedFields)
            );
        } else if (result instanceof List) {
            List<?> entityList = (List<?>) result;
            String operator = getCurrentUser();
            String description = buildDescription(recordUpdate, "创建");

            for (Object item : entityList) {
                if (item instanceof BaseEntity) {
                    BaseEntity entity = (BaseEntity) item;
                    Map<String, Object> processedFields = processEntityFieldsForLogging(entity);
                    updateRecordService.logCreate(
                            entity.getClass().getSimpleName(),
                            entity.getId(),
                            entity.get__name__(),
                            operator,
                            description,
                            serializeToJsonStr(processedFields)
                    );
                }
            }
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
            oldValues = processEntityFieldsForLogging(oldEntity);
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
                Map<String, Object> newValues = processEntityFieldsForLogging(newEntity);

                // 比较并获取变更字段
                Map<String, Object> changedOldValues = new HashMap<>();
                Map<String, Object> changedNewValues = new HashMap<>();

                if (oldValues != null) {
                    for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                        String fieldName = entry.getKey();
                        Object newValue = entry.getValue();
                        Object oldValue = oldValues.get(fieldName);

                        // 比较新旧值，只记录发生变化的字段
                        if (!ObjectCompareUtils.objectEqual(oldValue, newValue)) {
                            changedOldValues.put(fieldName, oldValue);
                            changedNewValues.put(fieldName, newValue);
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
                            serializeToJsonStr(changedOldValues),
                            serializeToJsonStr(changedNewValues),
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
        // 尝试获取要删除的实体信息
        BaseEntity oldEntity = extractOldEntityFromArgs(joinPoint, args);

        // 处理旧实体字段用于日志记录
        Map<String, Object> processedOldFields = null;
        if (oldEntity != null) {
            processedOldFields = processEntityFieldsForLogging(oldEntity);
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 记录删除操作
        if (oldEntity != null && processedOldFields != null) {
            String operator = getCurrentUser();
            String description = buildDescription(recordUpdate, "删除");
            updateRecordService.logDelete(
                    oldEntity.getClass().getSimpleName(),
                    oldEntity.getId(),
                    oldEntity.get__name__(),
                    operator,
                    description,
                    serializeToJsonStr(processedOldFields)
            );
        }

        return result;
    }

    /**
     * 处理批量拷贝操作
     */
    private Object handleBatchCopyOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        // 执行原方法
        Object result = joinPoint.proceed();

        // 获取最新版本号
        Long version = getLatestVersionFromResult(result);

        // 记录批量拷贝操作
        String operator = getCurrentUser();
        String description = buildDescription(recordUpdate, "批量拷贝");
        updateRecordService.logBatchCopy(operator, description, version);

        return result;
    }

    /**
     * 处理批量发布操作
     */
    private Object handleBatchPublishOperation(ProceedingJoinPoint joinPoint, RecordUpdate recordUpdate, Object[] args) throws Throwable {
        // 执行原方法
        Object result = joinPoint.proceed();

        // 获取最新版本号
        Long version = getLatestVersionFromResult(result);

        // 记录批量发布操作
        String operator = getCurrentUser();
        String description = buildDescription(recordUpdate, "批量发布");
        updateRecordService.logBatchPublish(operator, description, version);

        return result;
    }

    /**
     * 从批量操作结果中获取最新版本号
     */
    private Long getLatestVersionFromResult(Object result) {
        if (result instanceof List) {
            List<?> entityList = (List<?>) result;
            if (!entityList.isEmpty()) {
                Object firstItem = entityList.get(0);
                if (firstItem instanceof BaseEntity) {
                    BaseEntity entity = (BaseEntity) firstItem;
                    // 假设实体有一个获取版本号的方法，这里需要根据实际情况调整
                    // 如果实体中没有版本号字段，可以考虑其他方式获取版本号
                    try {
                        // 通过反射尝试获取version字段
                        Field versionField = entity.getClass().getDeclaredField("version");
                        versionField.setAccessible(true);
                        Object versionValue = versionField.get(entity);
                        if (versionValue instanceof Long) {
                            return (Long) versionValue;
                        } else if (versionValue instanceof Number) {
                            return ((Number) versionValue).longValue();
                        }
                    } catch (Exception e) {
                        logger.warning("无法获取实体版本号: " + e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从方法参数中提取旧实体状态
     */
    private BaseEntity extractOldEntityFromArgs(ProceedingJoinPoint joinPoint, Object[] args) {
        // 获取目标对象
        Object target = joinPoint.getTarget();

        // 如果有参数且第一个参数是Long类型，假设为ID参数
        if (args.length > 0 && args[0] instanceof Long) {
            Long id = (Long) args[0];

            // 获取目标类名
            String className = target.getClass().getSimpleName();

            try {
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

    /**
     * 计算对象的哈希值
     */
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

    /**
     * 根据类名确定要调用的方法名
     */
    private String determineMethodNameByClassName(String className) {
        // 根据不同的服务类确定对应的方法名
        switch (className) {
            case "MatterServiceImpl":
                return "getMatterById";
            case "MaterialServiceImpl":
                return "getMaterialById";
            case "ApprovalProcessDiagramServiceImpl":
                return "getDiagramById";
            case "BusinessProcessDiagramServiceImpl":
                return "getDiagramById";
            default:
                return null;
        }
    }

    /**
     * 处理实体字段用于日志记录，应用哈希和过滤规则
     */
    private Map<String, Object> processEntityFieldsForLogging(BaseEntity entity) {
        Map<String, Object> fieldValues = new HashMap<>();

        // 获取所有声明的字段（包括父类字段）
        List<Field> fields = new ArrayList<>();
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        // 遍历字段并获取值
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);

                // 对需要哈希处理的字段进行哈希处理
                if (HASH_FIELDS.contains(field.getName())) {
                    fieldValues.put(field.getName(), computeHash(value));
                } else {
                    fieldValues.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                logger.warning("无法访问字段: " + field.getName());
            }
        }

        return fieldValues;
    }

    /**
     * 序列化对象为JSON字符串
     */
    private String serializeToJsonStr(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warning("序列化对象时出错: " + e.getMessage());
            return obj.toString();
        }
    }
}
