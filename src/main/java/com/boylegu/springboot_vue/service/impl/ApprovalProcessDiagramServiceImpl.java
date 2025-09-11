// src/main/java/com/boylegu/springboot_vue/service/impl/ApprovalProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.repository.ApprovalProcessDiagramRepository;
import com.boylegu.springboot_vue.service.ApprovalProcessDiagramService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ApprovalProcessDiagramServiceImpl extends ProcessDiagramServiceImpl<ApprovalProcessDiagram, ApprovalProcessDiagramRepository> 
        implements ApprovalProcessDiagramService {

    public ApprovalProcessDiagramServiceImpl(ApprovalProcessDiagramRepository repository) {
        super(repository);
    }

    @Override
    public ApprovalProcessDiagram createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        ApprovalProcessDiagram diagram = createNewInstance();
        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
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
    public List<ApprovalProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return repository.findByIsValid(isValid);
    }

    @Override
    public ApprovalProcessDiagram activateDiagram(Long id) {
        ApprovalProcessDiagram diagram = super.activateDiagram(id);
        return diagram;
    }

    @Override
    public ApprovalProcessDiagram deactivateDiagram(Long id) {
        ApprovalProcessDiagram diagram = super.deactivateDiagram(id);
        return diagram;
    }

    @Override
    public ApprovalProcessDiagram createNewInstance() {
        return new ApprovalProcessDiagram();
    }
}