// src/main/java/com/boylegu/springboot_vue/service/impl/ApprovalProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.repository.ApprovalProcessDiagramRepository;
import com.boylegu.springboot_vue.service.ApprovalProcessDiagramService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ApprovalProcessDiagramServiceImpl extends ProcessDiagramServiceImpl<ApprovalProcessDiagram, ApprovalProcessDiagramRepository> 
        implements ApprovalProcessDiagramService {

    public ApprovalProcessDiagramServiceImpl(ApprovalProcessDiagramRepository repository) {
        super(repository);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建审批流程图")
    public ApprovalProcessDiagram createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        ApprovalProcessDiagram diagram = createNewInstance();
        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "更新审批流程图")
    public ApprovalProcessDiagram updateDiagram(Long id, String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        ApprovalProcessDiagram diagram = getDiagramById(id);
        if (diagram == null) {
            throw new RuntimeException("Diagram not found with id: " + id);
        }

        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除审批流程图")
    public void deleteDiagram(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ApprovalProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return repository.findByIsValid(isValid);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "激活审批流程图")
    public ApprovalProcessDiagram activateDiagram(Long id) {
        ApprovalProcessDiagram diagram = super.activateDiagram(id);
        return diagram;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "停用审批流程图")
    public ApprovalProcessDiagram deactivateDiagram(Long id) {
        ApprovalProcessDiagram diagram = super.deactivateDiagram(id);
        return diagram;
    }

    @Override
    public ApprovalProcessDiagram createNewInstance() {
        return new ApprovalProcessDiagram();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllDiagramVersions() {
        return repository.findAllVersions();
    }
}