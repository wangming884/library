package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reader")
public class Reader extends BaseEntity {
    private String cardNo;
    private String name;
    private Long typeId;
    private Integer gender;
    private String phone;
    private String email;
    private String dept;
    private String idCard;
    private String photo;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Integer status;
    private BigDecimal balance;
    private String remark;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String typeName;
    @TableField(exist = false)
    private Integer maxBorrow;
    @TableField(exist = false)
    private Integer maxDays;
    @TableField(exist = false)
    private Integer renewTimes;
    @TableField(exist = false)
    private Integer canBorrowRare;
}
