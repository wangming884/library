package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Announcement;
import com.library.security.LoginUser;
import com.library.service.AnnouncementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/api")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 获取公告列表（公开）
     */
    @GetMapping("/announcements")
    public Result<?> listAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type) {
        PageResult<Announcement> result = announcementService.listPage(page, size, type);
        return Result.success(result);
    }

    /**
     * 获取公告详情（公开）
     */
    @GetMapping("/announcements/{id}")
    public Result<?> getAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) return Result.error("公告不存在");
        announcementService.incrementView(id);
        return Result.success(announcement);
    }

    /**
     * 发布/编辑公告
     */
    @PostMapping("/admin/announcements")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> publish(@RequestBody Announcement announcement) {
        LoginUser user = getCurrentUser();
        if (user != null && announcement.getId() == null) {
            announcement.setAdminId(user.getUserId());
        }
        return announcementService.publish(announcement) ? Result.success("发布成功") : Result.error("发布失败");
    }

    /**
     * 置顶/取消置顶
     */
    @PutMapping("/admin/announcements/{id}/top")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> toggleTop(@PathVariable Long id) {
        try {
            return announcementService.toggleTop(id) ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
