// src/main/java/com/boylegu/springboot_vue/entities/BaseEntity.java
package com.boylegu.springboot_vue.entities;

import javax.persistence.*;
import java.util.Objects;

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

    // 版本号，用于乐观锁控制
    @Version
    @Column(name = "version")
    private Long version;

    // 是否有效（默认为true）
    @Column(name = "is_valid")
    private Boolean isValid = true;

    // 默认构造函数
    public BaseEntity() {}

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

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(__name__, that.__name__) &&
                Objects.equals(isValid, that.isValid) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, __name__, isValid, version);
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                ", __name__='" + __name__ + '\'' +
                ", isValid=" + isValid +
                ", version=" + version +
                '}';
    }
}
