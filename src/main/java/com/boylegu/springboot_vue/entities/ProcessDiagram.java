// src/main/java/com/boylegu/springboot_vue/entities/ProcessDiagram.java
package com.boylegu.springboot_vue.entities;

import com.fasterxml.jackson.annotation.*;
import javax.persistence.*;
import java.util.Base64;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProcessDiagram extends BaseEntity {
    @Column(name = "image_name", unique = true)
    private String imageName;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    // 使用 ImageType 枚举替代 contentType 字符串
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    // 默认构造函数
    public ProcessDiagram() {}

    // Getter和Setter方法
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
        updateName(); // 自动更新名称
    }

    @JsonIgnore
    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    // 获取 ImageType 枚举
    public ImageType getImageType() {
        return imageType;
    }

    // 设置 ImageType 枚举
    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    // 兼容旧的 getContentType 方法
    @Transient
    public String getContentType() {
        return imageType != null ? imageType.getContentType() : null;
    }

    // 兼容旧的 setContentType 方法
    @Transient
    public void setContentType(String contentType) {
        if (contentType != null) {
            this.imageType = ImageType.fromContentType(contentType);
        }
    }

    public void setVersion(Long version) {
        if (version != null) {
            super.setVersion(version);
        } else {
            // 如果传入 null，则保持当前值或设置默认值
            if (super.getVersion() == null) {
                super.setVersion(1L);
            }
        }
        updateName();  // 自动更新名称
    }

    public void updateName() {
        if (this.imageName != null && this.getVersion() != null) {
            this.set__name__(this.imageName + "_" + this.getVersion());
        } else if (this.imageName != null) {
            this.set__name__(this.imageName);
        } else {
            this.set__name__(null);
        }
    }

    // 获取图像数据的Base64 URL，用于前端显示
    @Transient
    @JsonProperty("imageDataUrl")
    public String getImageDataUrl() {
        if (imageData != null && imageType != null && imageData.length > 0) {
            return "data:" + imageType.getContentType() + ";base64," + Base64.getEncoder().encodeToString(imageData);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessDiagram that = (ProcessDiagram) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getImageName(), that.getImageName()) &&
                getImageType() == that.getImageType() &&
                Objects.equals(get__name__(), that.get__name__());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getImageName(), getImageType(), get__name__());
    }

    @Override
    public String toString() {
        return "ProcessDiagram{" +
                "id=" + getId() +
                ", imageName='" + getImageName() + '\'' +
                ", imageType=" + getImageType() +
                ", __name__='" + get__name__() + '\'' +
                ", isValid=" + getIsValid() +
                '}';
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
