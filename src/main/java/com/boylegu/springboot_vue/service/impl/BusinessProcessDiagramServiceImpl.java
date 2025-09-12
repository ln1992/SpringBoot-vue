// src/main/java/com/boylegu/springboot_vue/service/impl/BusinessProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.repository.BusinessProcessDiagramRepository;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BusinessProcessDiagramServiceImpl extends ProcessDiagramServiceImpl<BusinessProcessDiagram, BusinessProcessDiagramRepository>
        implements BusinessProcessDiagramService {

    @Autowired
    public BusinessProcessDiagramServiceImpl(BusinessProcessDiagramRepository repository) {
        super(repository);
    }


    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建业务流程图")
    public BusinessProcessDiagram createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        BusinessProcessDiagram diagram = createNewInstance();
        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "更新业务流程图")
    public BusinessProcessDiagram updateDiagram(Long id, String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        BusinessProcessDiagram diagram = getDiagramById(id);
        if (diagram == null) {
            throw new RuntimeException("Diagram not found with id: " + id);
        }

        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除业务流程图")
    public void deleteDiagram(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<BusinessProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return repository.findByIsValid(isValid);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "激活业务流程图")
    public BusinessProcessDiagram activateDiagram(Long id) {
        BusinessProcessDiagram diagram = super.activateDiagram(id);
        return diagram;
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "停用业务流程图")
    public BusinessProcessDiagram deactivateDiagram(Long id) {
        BusinessProcessDiagram diagram = super.deactivateDiagram(id);
        return diagram;
    }

    @Override
    public BusinessProcessDiagram createNewInstance() {
        return new BusinessProcessDiagram();
    }

}