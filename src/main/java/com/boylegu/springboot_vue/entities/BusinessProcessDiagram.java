// src/main/java/com/boylegu/springboot_vue/entities/BusinessProcessDiagram.java
package com.boylegu.springboot_vue.entities;

import javax.persistence.*;

@Entity
@Table(name = "business_process_diagram")
public class BusinessProcessDiagram extends ProcessDiagram {
    //业务经办流程图

    // 默认构造函数
    public BusinessProcessDiagram() {
        super();
    }
}
