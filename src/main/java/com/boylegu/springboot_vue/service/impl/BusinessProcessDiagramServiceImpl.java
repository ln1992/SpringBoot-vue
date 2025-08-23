// src/main/java/com/boylegu/springboot_vue/service/impl/BusinessProcessDiagramServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.repository.BusinessProcessDiagramRepository;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessProcessDiagramServiceImpl implements BusinessProcessDiagramService {

    @Autowired
    private BusinessProcessDiagramRepository businessProcessDiagramRepository;

    @Override
    public List<BusinessProcessDiagram> getAllDiagrams() {
        return businessProcessDiagramRepository.findAll();
    }

    @Override
    public BusinessProcessDiagram getDiagramById(Long id) {
        return businessProcessDiagramRepository.findById(id).orElse(null);
    }

    @Override
    public BusinessProcessDiagram saveDiagram(BusinessProcessDiagram diagram) {
        return businessProcessDiagramRepository.save(diagram);
    }

    @Override
    public BusinessProcessDiagram saveDiagramWithImage(MultipartFile imageFile, String imageName) throws IOException {
        BusinessProcessDiagram diagram = new BusinessProcessDiagram();
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

        return businessProcessDiagramRepository.save(diagram);
    }

    @Override
    public BusinessProcessDiagram updateDiagram(Long id, MultipartFile imageFile, String imageName) throws IOException {
        Optional<BusinessProcessDiagram> diagramOptional = businessProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            BusinessProcessDiagram diagram = diagramOptional.get();

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

            return businessProcessDiagramRepository.save(diagram);
        }
        return null;
    }

    @Override
    public void deleteDiagram(Long id) {
        businessProcessDiagramRepository.deleteById(id);
    }
}
