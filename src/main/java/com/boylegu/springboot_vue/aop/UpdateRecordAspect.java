// src/main/java/com/boylegu/springboot_vue/aop/UpdateRecordAspect.java
package com.boylegu.springboot_vue.aop;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.BaseEntity;
import com.boylegu.springboot_vue.service.UpdateRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 环绕通知，处理带有 @RecordUpdate 注解的方法
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
        BaseEntity oldEntityCopy = null;
        if (oldEntity != null) {
            oldEntityCopy = createEntityCopy(oldEntity);
        }
        
        try {
            // 执行原方法
            Object result = joinPoint.proceed();
            
            // 如果返回结果是 BaseEntity 或其子类，则记录更新操作
            if (result instanceof BaseEntity) {
                BaseEntity newEntity = (BaseEntity) result;
                String operator = getCurrentUser();
                String description = buildDescription(recordUpdate, "更新");
                
                if (oldEntityCopy != null) {
                    updateRecordService.logUpdate(oldEntityCopy, newEntity, operator, description);
                } else {
                    // 如果无法获取旧实体，则只记录新实体
                    updateRecordService.logCreate(newEntity, operator, description + " (无法获取更新前状态)");
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
        BaseEntity entityToDelete = extractEntityToDelete(args);
        
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
     * 创建实体副本
     */
    private BaseEntity createEntityCopy(BaseEntity original) {
        try {
            // 使用反射创建新实例
            BaseEntity copy = original.getClass().getDeclaredConstructor().newInstance();
            
            // 复制基本属性
            copy.setId(original.getId());
            copy.set__name__(original.get__name__());
            copy.setVersion(original.getVersion());
            copy.setCreatedTime(original.getCreatedTime());
            copy.setUpdateTime(original.getUpdateTime());
            copy.setValid(original.getValid());
            
            // 如果是Material实体，复制Material特有字段
            if (original instanceof com.boylegu.springboot_vue.entities.Material) {
                com.boylegu.springboot_vue.entities.Material originalMaterial = 
                    (com.boylegu.springboot_vue.entities.Material) original;
                com.boylegu.springboot_vue.entities.Material copyMaterial = 
                    (com.boylegu.springboot_vue.entities.Material) copy;
                
                copyMaterial.setMaterialDetail(originalMaterial.getMaterialDetail());
                copyMaterial.setReviewPoint(originalMaterial.getReviewPoint());
                copyMaterial.setAutoApprovalCriteria(originalMaterial.getAutoApprovalCriteria());
                copyMaterial.setShared(originalMaterial.getShared());
                copyMaterial.setMaterialSource(originalMaterial.getMaterialSource());
                copyMaterial.setProcessingMethodAndInfoAccess(originalMaterial.getProcessingMethodAndInfoAccess());
                copyMaterial.setEligibleForPromise(originalMaterial.getEligibleForPromise());
            }
            
            // 如果是Matter实体，复制Matter特有字段
            if (original instanceof com.boylegu.springboot_vue.entities.Matter) {
                com.boylegu.springboot_vue.entities.Matter originalMatter = 
                    (com.boylegu.springboot_vue.entities.Matter) original;
                com.boylegu.springboot_vue.entities.Matter copyMatter = 
                    (com.boylegu.springboot_vue.entities.Matter) copy;
                
                copyMatter.setMainItemCode(originalMatter.getMainItemCode());
                copyMatter.setSubItemCode(originalMatter.getSubItemCode());
                copyMatter.setGrandchildItemCode(originalMatter.getGrandchildItemCode());
                copyMatter.setMainItemName(originalMatter.getMainItemName());
                copyMatter.setSubItemName(originalMatter.getSubItemName());
                copyMatter.setGrandchildItemName(originalMatter.getGrandchildItemName());
                copyMatter.setBases(originalMatter.getBases());
                copyMatter.setMaterialIds(originalMatter.getMaterialIds());
                copyMatter.setLegalTimeLimit(originalMatter.getLegalTimeLimit());
                copyMatter.setCommittedTimeLimit(originalMatter.getCommittedTimeLimit());
                copyMatter.setApprovalLevel(originalMatter.getApprovalLevel());
                copyMatter.setProvincialDepartmentOffice(originalMatter.getProvincialDepartmentOffice());
                copyMatter.setApprovalProcessDiagramId(originalMatter.getApprovalProcessDiagramId());
                copyMatter.setBusinessProcessDiagramId(originalMatter.getBusinessProcessDiagramId());
                copyMatter.setPublish(originalMatter.getPublish());
            }
            
            // 如果是ProcessDiagram实体，复制ProcessDiagram特有字段
            if (original instanceof com.boylegu.springboot_vue.entities.ProcessDiagram) {
                com.boylegu.springboot_vue.entities.ProcessDiagram originalDiagram = 
                    (com.boylegu.springboot_vue.entities.ProcessDiagram) original;
                com.boylegu.springboot_vue.entities.ProcessDiagram copyDiagram = 
                    (com.boylegu.springboot_vue.entities.ProcessDiagram) copy;
                
                copyDiagram.setImageName(originalDiagram.getImageName());
                copyDiagram.setImageType(originalDiagram.getImageType());
                copyDiagram.setImageData(originalDiagram.getImageData());
            }
            
            return copy;
        } catch (Exception e) {
            logger.warning("无法创建实体副本: " + e.getMessage());
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
            String methodName = "";
            if (className.contains("Material")) {
                methodName = "getMaterialById";
            } else if (className.contains("Matter")) {
                methodName = "getMatterById";
            } else if (className.contains("ApprovalProcessDiagram")) {
                methodName = "getDiagramById";
            } else if (className.contains("BusinessProcessDiagram")) {
                methodName = "getDiagramById";
            } else {
                // 对于其他类型，暂时返回null
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
    private BaseEntity extractEntityToDelete(Object[] args) {
        // 查找参数中是否有 BaseEntity 或其子类
        for (Object arg : args) {
            if (arg instanceof BaseEntity) {
                return (BaseEntity) arg;
            }
        }
        
        // 如果参数中有ID（通常为Long类型），则尝试通过Repository查询实体
        // 这需要具体的Service配合实现
        for (Object arg : args) {
            if (arg instanceof Long) {
                // 无法通过ID直接获取实体，因为不知道具体是哪个实体类型
                // 需要在具体的方法中实现
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
}