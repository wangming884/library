package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate pubDate;
    private Long categoryId;
    private String categoryCode;
    private BigDecimal price;
    private String description;
    private String cover;
    private String keywords;
    private Integer totalCount;
    private Integer availableCount;
    private Integer isRare;
    private Integer status;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String categoryName;
}
