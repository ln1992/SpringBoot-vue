package com.boylegu.springboot_vue.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "approval_process_diagram")
public class ApprovalProcessDiagram extends ProcessDiagram {
    //审批流程图

    // 默认构造函数
    public ApprovalProcessDiagram() {
        super();
    }
}