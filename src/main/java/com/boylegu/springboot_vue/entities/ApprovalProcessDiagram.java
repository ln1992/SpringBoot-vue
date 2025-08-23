// src/main/java/com/boylegu/springboot_vue/entities/ApprovalProcessDiagram.java
package com.boylegu.springboot_vue.entities;

import javax.persistence.*;

@Entity
@Table(name = "approval_process_diagram")
public class ApprovalProcessDiagram extends ProcessDiagram {
    //审批流程图

    // 默认构造函数
    public ApprovalProcessDiagram() {
        super();
    }
}
