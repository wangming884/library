package com.library.service;

import com.library.entity.SysAdmin;
import java.util.Map;

public interface SysAdminService {

    /**
     * 管理员登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 根据ID查询（含角色信息）
     */
    SysAdmin getById(Long id);

    /**
     * 修改密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
}
