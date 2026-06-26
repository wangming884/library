package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Reader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReaderMapper extends BaseMapper<Reader> {

    @Select("SELECT r.*, rt.type_name, rt.max_borrow, rt.max_days, rt.renew_times FROM reader r " +
            "LEFT JOIN reader_type rt ON r.type_id = rt.id WHERE r.card_no = #{cardNo}")
    Reader findByCardNo(@Param("cardNo") String cardNo);

    @Select("SELECT r.*, rt.type_name, rt.max_borrow, rt.max_days, rt.renew_times FROM reader r " +
            "LEFT JOIN reader_type rt ON r.type_id = rt.id WHERE r.id = #{id}")
    Reader selectWithTypeInfo(@Param("id") Long id);
}
