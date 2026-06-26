package com.library.service.impl;

import com.library.entity.SysAdmin;
import com.library.entity.SysRole;
import com.library.mapper.SysAdminMapper;
import com.library.mapper.SysRoleMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.SysAdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SysAdminServiceImpl(SysAdminMapper adminMapper, SysRoleMapper roleMapper,
                                PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.adminMapper = adminMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        SysAdmin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录时间
        admin.setLastLogin(LocalDateTime.now());
        adminMapper.updateById(admin);

        // 获取角色
        SysRole role = roleMapper.selectById(admin.getRoleId());
        String roleKey = role != null ? role.getRoleKey() : "front_desk";

        // 生成Token
        String token = jwtTokenProvider.generateToken(admin.getId(), admin.getUsername(), roleKey);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", admin);
        result.put("roleKey", roleKey);
        result.put("roleName", role != null ? role.getRoleName() : "");
        return result;
    }

    @Override
    public SysAdmin getById(Long id) {
        return adminMapper.selectWithRole(id);
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        return adminMapper.updateById(admin) > 0;
    }
}
