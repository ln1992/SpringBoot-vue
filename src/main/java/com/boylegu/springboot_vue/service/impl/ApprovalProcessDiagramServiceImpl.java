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
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalProcessDiagramServiceImpl implements ApprovalProcessDiagramService {

    @Autowired
    private ApprovalProcessDiagramRepository approvalProcessDiagramRepository;

    @Override
    public List<ApprovalProcessDiagram> getAllDiagrams() {
        return approvalProcessDiagramRepository.findAll();
    }

    @Override
    public ApprovalProcessDiagram getDiagramById(Long id) {
        Optional<ApprovalProcessDiagram> diagram = approvalProcessDiagramRepository.findById(id);
        return diagram.orElse(null);
    }

    @Override
    public ApprovalProcessDiagram saveDiagram(ApprovalProcessDiagram diagram, MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                diagram.setImageData(imageFile.getBytes());

                // 使用 ImageType 设置 image type
                String originalFilename = imageFile.getOriginalFilename();
                if (originalFilename != null && originalFilename.contains(".")) {
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    ProcessDiagram.ImageType imageType = ProcessDiagram.ImageType.fromExtension(extension);
                    if (imageType != null) {
                        diagram.setImageType(imageType);
                    }
                }
            }
            return approvalProcessDiagramRepository.save(diagram);
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败", e);
        }
    }

    @Override
    public void deleteDiagram(Long id) {
        approvalProcessDiagramRepository.deleteById(id);
    }

    @Override
    public List<ApprovalProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return approvalProcessDiagramRepository.findByIsValid(isValid);
    }

    @Override
    public boolean activateDiagram(Long id) {
        Optional<ApprovalProcessDiagram> diagramOptional = approvalProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            ApprovalProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(true);
            approvalProcessDiagramRepository.save(diagram);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateDiagram(Long id) {
        Optional<ApprovalProcessDiagram> diagramOptional = approvalProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            ApprovalProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(false);
            approvalProcessDiagramRepository.save(diagram);
            return true;
        }
        return false;
    }
}