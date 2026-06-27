package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BookCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookCopyMapper extends BaseMapper<BookCopy> {

    @Select({
            "<script>",
            "SELECT bc.*, b.title AS book_title, b.isbn AS book_isbn",
            "FROM book_copy bc",
            "LEFT JOIN book b ON bc.book_id = b.id",
            "WHERE 1 = 1",
            "<if test='bookId != null'>AND bc.book_id = #{bookId}</if>",
            "<if test='status != null'>AND bc.status = #{status}</if>",
            "ORDER BY bc.create_time DESC",
            "</script>"
    })
    Page<BookCopy> selectCopyPage(Page<BookCopy> page, @Param("bookId") Long bookId, @Param("status") Integer status);

    @Select("SELECT bc.*, b.title as book_title, b.isbn as book_isbn FROM book_copy bc " +
            "LEFT JOIN book b ON bc.book_id = b.id WHERE bc.barcode = #{barcode}")
    BookCopy findByBarcode(@Param("barcode") String barcode);
}
