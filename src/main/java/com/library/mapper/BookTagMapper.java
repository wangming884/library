package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookTagMapper extends BaseMapper<BookTag> {

    @Select("SELECT COUNT(*) FROM book_tag_rel WHERE book_id = #{bookId}")
    long countRelationsByBookId(@Param("bookId") Long bookId);

    @Delete("DELETE FROM book_tag_rel WHERE book_id = #{bookId}")
    int deleteRelationsByBookId(@Param("bookId") Long bookId);

    @Select("SELECT COUNT(*) FROM book_tag_rel WHERE tag_id = #{tagId}")
    long countRelationsByTagId(@Param("tagId") Long tagId);

    @Delete("DELETE FROM book_tag_rel WHERE tag_id = #{tagId}")
    int deleteRelationsByTagId(@Param("tagId") Long tagId);
}
