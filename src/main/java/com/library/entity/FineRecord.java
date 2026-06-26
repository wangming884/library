package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fine_record")
public class FineRecord extends BaseEntity {
    private Long readerId;
    private Long borrowRecordId;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    private String payMethod;
    private Long operatorId;
    private String remark;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String readerName;
    @TableField(exist = false)
    private String readerCardNo;
    @TableField(exist = false)
    private String bookTitle;
    @TableField(exist = false)
    private String operatorName;
}
