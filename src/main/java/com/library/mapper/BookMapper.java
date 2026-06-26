package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Select("SELECT b.*, c.name as category_name FROM book b LEFT JOIN category c ON b.category_id = c.id WHERE b.id = #{id}")
    Book selectWithCategory(@Param("id") Long id);

    /**
     * 借阅排行榜
     */
    @Select("SELECT b.id, b.title, b.author, b.cover, b.isbn, COUNT(br.id) as borrow_count " +
            "FROM borrow_record br JOIN book_copy bc ON br.copy_id = bc.id JOIN book b ON bc.book_id = b.id " +
            "GROUP BY b.id ORDER BY borrow_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getBorrowRank(@Param("limit") int limit);
}
