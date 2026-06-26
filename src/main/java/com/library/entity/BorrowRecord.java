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
@TableName("borrow_record")
public class BorrowRecord extends BaseEntity {
    private Long readerId;
    private Long copyId;
    private Long bookId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;
    private Integer renewCount;
    private Integer status;
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
    private String bookIsbn;
    @TableField(exist = false)
    private String copyBarcode;
    @TableField(exist = false)
    private String operatorName;
}
