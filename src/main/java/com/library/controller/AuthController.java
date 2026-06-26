package com.library.controller;

import com.library.common.Result;
import com.library.entity.SysAdmin;
import com.library.security.JwtTokenProvider;
import com.library.security.LoginUser;
import com.library.service.SysAdminService;
import com.library.service.ReaderAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysAdminService adminService;
    private final ReaderAuthService readerAuthService;

    public AuthController(SysAdminService adminService, ReaderAuthService readerAuthService) {
        this.adminService = adminService;
        this.readerAuthService = readerAuthService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            Map<String, Object> result = adminService.login(username, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者登录
     */
    @PostMapping("/reader-login")
    public Result<?> readerLogin(@RequestBody Map<String, String> params) {
        String cardNo = params.get("cardNo");
        String password = params.get("password");
        if (cardNo == null || password == null) {
            return Result.error("证件号和密码不能为空");
        }
        try {
            Map<String, Object> result = readerAuthService.readerLogin(cardNo, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录信息
     */
    @GetMapping("/info")
    public Result<?> getInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            return Result.error(401, "未登录");
        }
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        SysAdmin admin = adminService.getById(loginUser.getUserId());
        if (admin != null) {
            admin.setPassword(null);
        }
        return Result.success(admin);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        try {
            adminService.changePassword(loginUser.getUserId(), oldPwd, newPwd);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
