package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Reader;
import com.library.entity.ReaderType;
import com.library.security.LoginUser;
import com.library.service.ReaderAuthService;
import com.library.service.ReaderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 读者管理控制器
 */
@RestController
@RequestMapping("/api")
public class ReaderController {

    private final ReaderService readerService;
    private final ReaderAuthService readerAuthService;

    public ReaderController(ReaderService readerService, ReaderAuthService readerAuthService) {
        this.readerService = readerService;
        this.readerAuthService = readerAuthService;
    }

    // ==================== 读者注册 ====================

    @PostMapping("/reader/register")
    public Result<?> register(@RequestBody Reader reader) {
        try {
            readerAuthService.register(reader);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 管理员操作 ====================

    @GetMapping("/admin/readers")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> listReaders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long typeId) {
        PageResult<Reader> result = readerService.listPage(page, size, keyword, status, typeId);
        return Result.success(result);
    }

    @GetMapping("/admin/readers/{id}")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> getReader(@PathVariable Long id) {
        Reader reader = readerService.getDetail(id);
        if (reader == null) return Result.error("读者不存在");
        reader.setPassword(null);
        return Result.success(reader);
    }

    @PostMapping("/admin/readers")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> addReader(@RequestBody Reader reader) {
        try {
            readerAuthService.register(reader);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> updateReader(@PathVariable Long id, @RequestBody Reader reader) {
        reader.setId(id);
        reader.setPassword(null); // 不修改密码
        try {
            return readerService.updateReader(reader) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/loss")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> reportLoss(@PathVariable Long id) {
        try {
            return readerService.reportLoss(id) ? Result.success("挂失成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/reissue")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> reissue(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            return readerService.reissue(id, params.get("newCardNo")) ? Result.success("补办成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/freeze")
    @PreAuthorize("hasAnyRole('super_admin')")
    public Result<?> freeze(@PathVariable Long id) {
        try {
            return readerService.freeze(id) ? Result.success("冻结成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/unfreeze")
    @PreAuthorize("hasAnyRole('super_admin')")
    public Result<?> unfreeze(@PathVariable Long id) {
        try {
            return readerService.unfreeze(id) ? Result.success("解冻成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/blacklist")
    @PreAuthorize("hasAnyRole('super_admin')")
    public Result<?> addToBlacklist(@PathVariable Long id) {
        try {
            return readerService.addToBlacklist(id) ? Result.success("已加入黑名单") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/readers/{id}/remove-blacklist")
    @PreAuthorize("hasAnyRole('super_admin')")
    public Result<?> removeFromBlacklist(@PathVariable Long id) {
        try {
            return readerService.removeFromBlacklist(id) ? Result.success("已移出黑名单") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 读者类型 ====================

    @GetMapping("/reader-types")
    public Result<?> publicReaderTypes() {
        return Result.success(readerService.listReaderTypes().stream()
                .filter(type -> type.getStatus() != null && type.getStatus() == 1)
                .toList());
    }

    @GetMapping("/admin/reader-types")
    public Result<?> listReaderTypes() {
        return Result.success(readerService.listReaderTypes());
    }

    @PostMapping("/admin/reader-types")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> addReaderType(@RequestBody ReaderType type) {
        try {
            return readerService.saveReaderType(type) ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/reader-types/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> updateReaderType(@PathVariable Long id, @RequestBody ReaderType type) {
        type.setId(id);
        try {
            return readerService.updateReaderType(type) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 读者个人中心 ====================

    @GetMapping("/reader/profile")
    public Result<?> getProfile() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) return Result.error(401, "未登录");
        Reader reader = readerService.getDetail(loginUser.getUserId());
        if (reader == null) return Result.error("读者不存在");
        reader.setPassword(null);
        return Result.success(reader);
    }

    @PutMapping("/reader/profile")
    public Result<?> updateProfile(@RequestBody Reader reader) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) return Result.error(401, "未登录");
        reader.setId(loginUser.getUserId());
        reader.setPassword(null); // 不修改密码
        reader.setCardNo(null); // 不允许修改证件号
        reader.setStatus(null); // 不允许修改状态
        reader.setTypeId(null); // 不允许修改读者类型
        try {
            return readerService.updateReader(reader) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/reader/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) return Result.error(401, "未登录");
        // 读者修改密码逻辑
        Reader reader = readerService.getById(loginUser.getUserId());
        if (reader == null) return Result.error("读者不存在");
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        if (!StringUtils.hasText(oldPwd)) {
            return Result.error("请输入原密码");
        }
        if (!StringUtils.hasText(newPwd)) {
            return Result.error("请输入新密码");
        }
        if (!org.springframework.security.crypto.bcrypt.BCrypt.checkpw(oldPwd, reader.getPassword())) {
            return Result.error("原密码错误");
        }
        reader.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPwd.trim()));
        return readerService.updateById(reader) ? Result.success("密码修改成功") : Result.error("修改失败");
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
