package com.library.security;

import com.library.entity.SysAdmin;
import com.library.mapper.SysAdminMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysAdminMapper adminMapper;

    public UserDetailsServiceImpl(SysAdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysAdmin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (admin.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities("ROLE_admin")
                .build();
    }
}
