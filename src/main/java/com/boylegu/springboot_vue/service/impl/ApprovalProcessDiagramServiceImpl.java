// src/main/java/com/boylegu/springboot_vue/service/impl/ApprovalProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
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
        return approvalProcessDiagramRepository.findById(id).orElse(null);
    }

    @Override
    public ApprovalProcessDiagram saveDiagram(ApprovalProcessDiagram diagram) {
        return approvalProcessDiagramRepository.save(diagram);
    }

    @Override
    public ApprovalProcessDiagram saveDiagramWithImage(MultipartFile imageFile, String imageName) throws IOException {
        ApprovalProcessDiagram diagram = new ApprovalProcessDiagram();
        diagram.setImageName(imageName);
        diagram.setImageData(imageFile.getBytes());

        // 设置content type
        String contentType = imageFile.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            // 根据文件扩展名推断content type
            String originalFilename = imageFile.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                com.boylegu.springboot_vue.entities.ProcessDiagram.ImageType imageType =
                        com.boylegu.springboot_vue.entities.ProcessDiagram.ImageType.fromExtension(extension);
                if (imageType != null) {
                    contentType = imageType.getContentType();
                }
            }
        }
        diagram.setContentType(contentType);

        return approvalProcessDiagramRepository.save(diagram);
    }

    @Override
    public ApprovalProcessDiagram updateDiagram(Long id, MultipartFile imageFile, String imageName) throws IOException {
        Optional<ApprovalProcessDiagram> diagramOptional = approvalProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            ApprovalProcessDiagram diagram = diagramOptional.get();

            if (imageName != null && !imageName.isEmpty()) {
                diagram.setImageName(imageName);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                diagram.setImageData(imageFile.getBytes());

                // 设置content type
                String contentType = imageFile.getContentType();
                if (contentType == null || contentType.isEmpty()) {
                    // 根据文件扩展名推断content type
                    String originalFilename = imageFile.getOriginalFilename();
                    if (originalFilename != null && originalFilename.contains(".")) {
                        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        com.boylegu.springboot_vue.entities.ProcessDiagram.ImageType imageType =
                                com.boylegu.springboot_vue.entities.ProcessDiagram.ImageType.fromExtension(extension);
                        if (imageType != null) {
                            contentType = imageType.getContentType();
                        }
                    }
                }
                diagram.setContentType(contentType);
            }

            return approvalProcessDiagramRepository.save(diagram);
        }
        return null;
    }

    @Override
    public void deleteDiagram(Long id) {
        approvalProcessDiagramRepository.deleteById(id);
    }
}
