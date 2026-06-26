package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookCopyMapper extends BaseMapper<BookCopy> {

    @Select("SELECT bc.*, b.title as book_title, b.isbn as book_isbn FROM book_copy bc " +
            "LEFT JOIN book b ON bc.book_id = b.id WHERE bc.barcode = #{barcode}")
    BookCopy findByBarcode(@Param("barcode") String barcode);
}
