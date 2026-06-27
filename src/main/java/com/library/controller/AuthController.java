package com.library.controller;

import com.library.common.Result;
import com.library.entity.Reader;
import com.library.entity.SysAdmin;
import com.library.security.LoginUser;
import com.library.service.ReaderService;
import com.library.service.SysAdminService;
import com.library.service.ReaderAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysAdminService adminService;
    private final ReaderAuthService readerAuthService;
    private final ReaderService readerService;

    public AuthController(SysAdminService adminService, ReaderAuthService readerAuthService,
                          ReaderService readerService) {
        this.adminService = adminService;
        this.readerAuthService = readerAuthService;
        this.readerService = readerService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            Map<String, Object> result = adminService.login(username.trim(), password);
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
        if (!StringUtils.hasText(cardNo) || !StringUtils.hasText(password)) {
            return Result.error("证件号和密码不能为空");
        }
        try {
            Map<String, Object> result = readerAuthService.readerLogin(cardNo.trim(), password);
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
        if ("reader".equals(loginUser.getRoleKey())) {
            Reader reader = readerService.getDetail(loginUser.getUserId());
            if (reader == null) return Result.error("读者不存在");
            reader.setPassword(null);
            return Result.success(Map.of(
                    "user", reader,
                    "reader", reader,
                    "roleKey", "reader"
            ));
        }
        SysAdmin admin = adminService.getById(loginUser.getUserId());
        if (admin == null) return Result.error("管理员不存在");
        if (admin != null) {
            admin.setPassword(null);
        }
        return Result.success(Map.of(
                "user", admin,
                "admin", admin,
                "roleKey", loginUser.getRoleKey()
        ));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        if ("reader".equals(loginUser.getRoleKey())) {
            return Result.error(403, "读者请在个人中心修改密码");
        }
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        if (!StringUtils.hasText(oldPwd)) {
            return Result.error("请输入原密码");
        }
        if (!StringUtils.hasText(newPwd)) {
            return Result.error("请输入新密码");
        }
        try {
            adminService.changePassword(loginUser.getUserId(), oldPwd, newPwd.trim());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
