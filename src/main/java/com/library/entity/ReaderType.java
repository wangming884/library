package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reader_type")
public class ReaderType extends BaseEntity {
    private String typeName;
    private String typeKey;
    private Integer maxBorrow;
    private Integer maxDays;
    private Integer renewTimes;
    private Integer canBorrowRare;
    private String description;
    private Integer status;
}
