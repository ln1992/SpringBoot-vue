// src/main/java/com/boylegu/springboot_vue/service/impl/BusinessProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.repository.BusinessProcessDiagramRepository;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
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
public class BusinessProcessDiagramServiceImpl extends ProcessDiagramServiceImpl<BusinessProcessDiagram, BusinessProcessDiagramRepository> 
        implements BusinessProcessDiagramService {

    @Autowired
    public BusinessProcessDiagramServiceImpl(BusinessProcessDiagramRepository repository) {
        super(repository);
    }

    @Override
    public List<BusinessProcessDiagram> getAllDiagrams() {
        return super.getAllDiagrams();
    }

    @Override
    public BusinessProcessDiagram getDiagramById(Long id) {
        return super.getDiagramById(id);
    }

    @Override
    public BusinessProcessDiagram createDiagram(String imageName, Long version, MultipartFile imageFile, Boolean isValid) {
        BusinessProcessDiagram diagram = createNewInstance();
        diagram.setImageName(imageName);
        diagram.setVersion(version);
        diagram.setValid(isValid);
        return saveDiagram(diagram, imageFile);
    }

    @Override
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
    public void deleteDiagram(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<BusinessProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return repository.findByIsValid(isValid);
    }

    @Override
    public BusinessProcessDiagram activateDiagram(Long id) {
        Optional<BusinessProcessDiagram> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            BusinessProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(true);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public BusinessProcessDiagram deactivateDiagram(Long id) {
        Optional<BusinessProcessDiagram> diagramOptional = repository.findById(id);
        if (diagramOptional.isPresent()) {
            BusinessProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(false);
            return repository.save(diagram);
        }
        return null;
    }

    @Override
    public BusinessProcessDiagram createNewInstance() {
        return new BusinessProcessDiagram();
    }

    public Map<Long, BusinessProcessDiagram> getBusinessDiagramsMapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        List<BusinessProcessDiagram> diagrams = repository.findAllById(ids);
        return diagrams.stream()
                .collect(Collectors.toMap(BusinessProcessDiagram::getId, diagram -> diagram));
    }
}