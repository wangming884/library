package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BookReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookReviewMapper extends BaseMapper<BookReview> {

    @Select({
            "<script>",
            "SELECT br.*, r.name AS reader_name, b.title AS book_title",
            "FROM book_review br",
            "LEFT JOIN reader r ON br.reader_id = r.id",
            "LEFT JOIN book b ON br.book_id = b.id",
            "WHERE br.book_id = #{bookId}",
            "AND br.status = 1",
            "ORDER BY br.create_time DESC",
            "</script>"
    })
    Page<BookReview> selectReviewPage(Page<BookReview> page, @Param("bookId") Long bookId);

    @Select("SELECT br.*, r.name AS reader_name, b.title AS book_title " +
            "FROM book_review br " +
            "LEFT JOIN reader r ON br.reader_id = r.id " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "WHERE br.id = #{id}")
    BookReview selectWithDetails(@Param("id") Long id);
}
