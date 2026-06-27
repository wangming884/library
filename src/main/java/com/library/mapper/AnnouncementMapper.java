package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select({
            "<script>",
            "SELECT an.*, a.real_name AS admin_name",
            "FROM announcement an",
            "LEFT JOIN sys_admin a ON an.admin_id = a.id",
            "WHERE an.status = 1",
            "<if test='type != null'>AND an.type = #{type}</if>",
            "ORDER BY an.is_top DESC, an.publish_time DESC",
            "</script>"
    })
    Page<Announcement> selectAnnouncementPage(Page<Announcement> page, @Param("type") Integer type);
}
