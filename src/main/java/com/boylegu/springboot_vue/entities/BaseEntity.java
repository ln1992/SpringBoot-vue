// src/main/java/com/boylegu/springboot_vue/entities/BaseEntity.java
package com.boylegu.springboot_vue.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * 实体基类，包含所有实体共有的字段
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 使用IDENTITY策略
    private Long id;

    // 名字+版本号
    @Column(name = "__name__")
    private String __name__;

    @Column(name = "version")
    private Long version;

    // 是否有效（默认为true）
    @Column(name = "is_valid")
    private Boolean isValid = true;

    // 创建时间
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    // 更新时间
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // 默认构造函数
    public BaseEntity() {}

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = new Date();
        }
        if (updateTime == null) {
            updateTime = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String get__name__() {
        return __name__;
    }

    public void set__name__(String __name__) {
        this.__name__ = __name__;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(__name__, that.__name__) &&
                Objects.equals(isValid, that.isValid) &&
                Objects.equals(version, that.version) &&
                Objects.equals(createdTime, that.createdTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, __name__, isValid, version, createdTime, updateTime);
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                ", __name__='" + __name__ + '\'' +
                ", isValid=" + isValid +
                ", version=" + version +
                ", createdTime=" + createdTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
