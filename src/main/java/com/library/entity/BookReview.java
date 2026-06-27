package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_review")
public class BookReview extends BaseEntity {
    private Long bookId;
    private Long readerId;
    private Integer rating;
    private String content;
    private Integer status;

    @TableField(exist = false)
    private String readerName;
    @TableField(exist = false)
    private String bookTitle;
}
