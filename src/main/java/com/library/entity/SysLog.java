package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
public class SysLog extends BaseEntity {
    private Long adminId;
    private String adminName;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long duration;
    private Integer status;
}
