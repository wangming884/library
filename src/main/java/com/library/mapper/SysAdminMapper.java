package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    @Select("SELECT a.*, r.role_name, r.role_key FROM sys_admin a LEFT JOIN sys_role r ON a.role_id = r.id WHERE a.username = #{username}")
    SysAdmin findByUsername(@Param("username") String username);

    @Select("SELECT a.*, r.role_name, r.role_key FROM sys_admin a LEFT JOIN sys_role r ON a.role_id = r.id WHERE a.id = #{id}")
    SysAdmin selectWithRole(@Param("id") Long id);
}
