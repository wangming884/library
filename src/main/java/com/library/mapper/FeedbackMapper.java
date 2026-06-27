package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    @Select({
            "<script>",
            "SELECT f.*, r.name AS reader_name, r.card_no AS reader_card_no, a.real_name AS admin_name",
            "FROM feedback f",
            "LEFT JOIN reader r ON f.reader_id = r.id",
            "LEFT JOIN sys_admin a ON f.admin_id = a.id",
            "WHERE 1 = 1",
            "<if test='status != null'>AND f.status = #{status}</if>",
            "<if test='readerId != null'>AND f.reader_id = #{readerId}</if>",
            "ORDER BY f.create_time DESC",
            "</script>"
    })
    Page<Feedback> selectFeedbackPage(Page<Feedback> page,
                                      @Param("status") Integer status,
                                      @Param("readerId") Long readerId);
}
