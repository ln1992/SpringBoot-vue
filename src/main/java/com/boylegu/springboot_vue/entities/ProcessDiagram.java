// src/main/java/com/boylegu/springboot_vue/entities/BaseProcessDiagram.java
package com.boylegu.springboot_vue.entities;

import javax.persistence.*;

@MappedSuperclass
public abstract class ProcessDiagram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "content_type")
    private String contentType;

    // 默认构造函数
    public ProcessDiagram() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    // 使用 ImageType 设置 content type
    public void setImageType(ImageType imageType) {
        if (imageType != null) {
            this.contentType = imageType.getContentType();
        }
    }

    // 根据 content type 获取 ImageType
    public ImageType getImageType() {
        return ImageType.fromContentType(this.contentType);
    }

    public enum ImageType {
        JPEG("image/jpeg", ".jpg"),
        PNG("image/png", ".png"),
        GIF("image/gif", ".gif"),
        BMP("image/bmp", ".bmp"),
        SVG("image/svg+xml", ".svg");

        private final String contentType;
        private final String extension;

        ImageType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        public String getContentType() {
            return contentType;
        }

        public String getExtension() {
            return extension;
        }

        /**
         * 根据文件扩展名获取对应的ImageType
         * @param extension 文件扩展名
         * @return 对应的ImageType，如果找不到返回null
         */
        public static ImageType fromExtension(String extension) {
            if (extension == null || extension.isEmpty()) {
                return null;
            }

            // 确保扩展名以点开头
            String ext = extension.startsWith(".") ? extension.toLowerCase() : "." + extension.toLowerCase();

            for (ImageType type : ImageType.values()) {
                if (type.getExtension().equals(ext)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 根据content type获取对应的ImageType
         * @param contentType content type字符串
         * @return 对应的ImageType，如果找不到返回null
         */
        public static ImageType fromContentType(String contentType) {
            if (contentType == null || contentType.isEmpty()) {
                return null;
            }

            for (ImageType type : ImageType.values()) {
                if (type.getContentType().equals(contentType.toLowerCase())) {
                    return type;
                }
            }
            return null;
        }
    }
}
