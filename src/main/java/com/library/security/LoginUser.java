package com.library.security;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录用户信息
 */
@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String roleKey;

    public LoginUser() {}

    public LoginUser(Long userId, String username, String roleKey) {
        this.userId = userId;
        this.username = username;
        this.roleKey = roleKey;
    }
}
