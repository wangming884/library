package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Feedback;
import com.library.security.LoginUser;
import com.library.service.FeedbackService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 留言反馈控制器
 */
@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 读者提交反馈
     */
    @PostMapping("/reader/feedback")
    public Result<?> submitFeedback(@RequestBody Feedback feedback) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        feedback.setReaderId(user.getUserId());
        return feedbackService.submit(feedback) ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 读者查看自己的反馈
     */
    @GetMapping("/reader/feedback")
    public Result<?> myFeedback(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        PageResult<Feedback> result = feedbackService.listPage(page, size, null, user.getUserId());
        return Result.success(result);
    }

    /**
     * 管理员查看所有反馈
     */
    @GetMapping("/admin/feedback")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> listFeedback(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        PageResult<Feedback> result = feedbackService.listPage(page, size, status, null);
        return Result.success(result);
    }

    /**
     * 管理员回复反馈
     */
    @PostMapping("/admin/feedback/{id}/reply")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> replyFeedback(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser user = getCurrentUser();
        try {
            return feedbackService.reply(id, params.get("reply"), user != null ? user.getUserId() : null)
                    ? Result.success("回复成功") : Result.error("回复失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
