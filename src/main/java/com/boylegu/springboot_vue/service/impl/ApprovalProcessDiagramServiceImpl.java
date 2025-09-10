// src/main/java/com/boylegu/springboot_vue/service/impl/ApprovalProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.repository.ApprovalProcessDiagramRepository;
import com.boylegu.springboot_vue.service.ApprovalProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApprovalProcessDiagramServiceImpl extends ProcessDiagramServiceImpl<ApprovalProcessDiagram, ApprovalProcessDiagramRepository> 
        implements ApprovalProcessDiagramService {

    @Autowired
    public ApprovalProcessDiagramServiceImpl(ApprovalProcessDiagramRepository repository) {
        super(repository);
    }

    @Override
    public List<ApprovalProcessDiagram> getAllDiagrams() {
        return super.getAllDiagrams();
    }

    @Override
    public ApprovalProcessDiagram getDiagramById(Long id) {
        return super.getDiagramById(id);
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
    public void deleteDiagram(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ApprovalProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return repository.findByIsValid(isValid);
    }

    @Override
    public ApprovalProcessDiagram activateDiagram(Long id) {
        Optional<ApprovalProcessDiagram> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            ApprovalProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(true);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public ApprovalProcessDiagram deactivateDiagram(Long id) {
        Optional<ApprovalProcessDiagram> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            ApprovalProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(false);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public ApprovalProcessDiagram createNewInstance() {
        return new ApprovalProcessDiagram();
    }

    public Map<Long, ApprovalProcessDiagram> getApprovalDiagramsMapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        List<ApprovalProcessDiagram> diagrams = repository.findAllById(ids);
        return diagrams.stream()
                .collect(Collectors.toMap(ApprovalProcessDiagram::getId, diagram -> diagram));
    }
}