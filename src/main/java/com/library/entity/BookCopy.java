package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_copy")
public class BookCopy extends BaseEntity {
    private Long bookId;
    private String barcode;
    private String location;
    private String floor;
    private String shelf;
    private Integer status;
    private String remark;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String bookTitle;
    @TableField(exist = false)
    private String bookIsbn;
}
