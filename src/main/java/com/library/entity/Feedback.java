package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feedback")
public class Feedback extends BaseEntity {
    private Long readerId;
    private Integer type;
    private String title;
    private String content;
    private String reply;
    private Long adminId;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String readerName;
    @TableField(exist = false)
    private String readerCardNo;
    @TableField(exist = false)
    private String adminName;
}
