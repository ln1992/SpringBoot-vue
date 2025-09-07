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
        Optional<BusinessProcessDiagram> diagram = businessProcessDiagramRepository.findById(id);
        return diagram.orElse(null);
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

    // 这是一个额外的公共方法，不是接口中定义的，用于保存图表和处理文件上传
    public BusinessProcessDiagram saveDiagram(BusinessProcessDiagram diagram, MultipartFile imageFile) {
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
            return businessProcessDiagramRepository.save(diagram);
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败", e);
        }
    }

    @Override
    public void deleteDiagram(Long id) {
        businessProcessDiagramRepository.deleteById(id);
    }

    @Override
    public List<BusinessProcessDiagram> getDiagramsByIsValid(Boolean isValid) {
        return businessProcessDiagramRepository.findByIsValid(isValid);
    }

    @Override
    public boolean activateDiagram(Long id) {
        Optional<BusinessProcessDiagram> diagramOptional = businessProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            BusinessProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(true);
            businessProcessDiagramRepository.save(diagram);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateDiagram(Long id) {
        Optional<BusinessProcessDiagram> diagramOptional = businessProcessDiagramRepository.findById(id);
        if (diagramOptional.isPresent()) {
            BusinessProcessDiagram diagram = diagramOptional.get();
            diagram.setValid(false);
            businessProcessDiagramRepository.save(diagram);
            return true;
        }
        return false;
    }

    @Override
    public BusinessProcessDiagram createNewInstance() {
        return new BusinessProcessDiagram();
    }
}
