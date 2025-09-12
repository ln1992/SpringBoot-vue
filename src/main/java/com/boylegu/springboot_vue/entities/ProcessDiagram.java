// src/main/java/com/boylegu/springboot_vue/entities/ProcessDiagram.java
package com.boylegu.springboot_vue.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.util.Base64;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"image_name", "version"}))
public abstract class ProcessDiagram extends BaseEntity {
    @Column(name = "image_name")
    private String imageName;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    // 使用 ImageType 枚举
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    // 缓存Base64编码的图像URL以提高性能
    @Transient
    private String cachedImageDataUrl;

    // 常量定义
    @Transient
    private static final int MAX_IMAGE_SIZE_FOR_BASE64 = 5 * 1024 * 1024; // 5MB
    @Transient
    private static final String DATA_URL_PREFIX = "data:";
    @Transient
    private static final String BASE64_PREFIX = ";base64,";

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
        this.cachedImageDataUrl = null; // 清除缓存
    }

    // 获取 ImageType 枚举
    public ImageType getImageType() {
        return imageType;
    }

    // 设置 ImageType 枚举
    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
        this.cachedImageDataUrl = null; // 清除缓存
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
        // 检查缓存是否仍然有效
        if (cachedImageDataUrl != null && !isImageDataChanged()) {
            return cachedImageDataUrl;
        }

        try {
            if (isValidForBase64Encoding()) {
                cachedImageDataUrl = buildDataUrl();
                return cachedImageDataUrl;
            }
        } catch (Exception e) {
            // 出现异常时清除缓存并返回null
            cachedImageDataUrl = null;
            return null;
        }

        // 无效情况下清除缓存
        cachedImageDataUrl = null;
        return null;
    }

    /**
     * 检查图像数据是否发生变化
     * @return 如果数据发生变化返回true，否则返回false
     */
    private boolean isImageDataChanged() {
        return cachedImageDataUrl != null &&
                (imageData == null || imageType == null);
    }

    /**
     * 验证图像数据是否适合进行Base64编码
     * @return 如果适合编码返回true，否则返回false
     */
    private boolean isValidForBase64Encoding() {
        return imageData != null &&
                imageType != null &&
                imageData.length > 0 &&
                imageData.length <= MAX_IMAGE_SIZE_FOR_BASE64;
    }

    /**
     * 构建Data URL
     * @return Data URL字符串
     */
    private String buildDataUrl() {
        return DATA_URL_PREFIX +
                imageType.getContentType() +
                BASE64_PREFIX +
                Base64.getEncoder().encodeToString(imageData);
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
        return "ProcessDiagram{ " +
                super.toString() +  // 包含父类的所有字段
                ", imageName='" + imageName + '\'' +
                ", imageType=" + imageType +
                ", __name__='" + get__name__() + '\'' +
                ", isValid=" + getValid() +
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
