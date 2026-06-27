package com.library.controller;

import com.library.common.Constants;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.security.LoginUser;
import com.library.service.BorrowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 借阅流通控制器
 */
@RestController
@RequestMapping("/api")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    // ==================== 管理员操作 ====================

    /**
     * 借书办理
     */
    @PostMapping("/admin/borrow")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> borrow(@RequestBody Map<String, Long> params) {
        Long readerId = params.get("readerId");
        Long copyId = params.get("copyId");
        Long bookId = params.get("bookId");
        LoginUser user = getCurrentUser();
        try {
            BorrowRecord record = copyId != null
                    ? borrowService.borrow(readerId, copyId, user != null ? user.getUserId() : null)
                    : borrowService.borrowAvailableCopy(readerId, bookId, user != null ? user.getUserId() : null);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 还书办理
     */
    @PostMapping("/admin/return/{id}")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> returnBook(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        try {
            BorrowRecord record = borrowService.returnBook(id, user != null ? user.getUserId() : null);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 续借
     */
    @PostMapping("/admin/renew/{id}")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> renew(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        try {
            String msg = borrowService.renew(id, user != null ? user.getUserId() : null);
            return Result.success(msg);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 借阅记录查询
     */
    @GetMapping("/admin/borrow-records")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> listBorrowRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<BorrowRecord> result = borrowService.listBorrowRecords(page, size, readerId, status, keyword);
        return Result.success(result);
    }

    /**
     * 预约记录查询
     */
    @GetMapping("/admin/reservations")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> listReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(borrowService.listReservations(page, size, readerId, status, keyword));
    }

    /**
     * 检测逾期
     */
    @PostMapping("/admin/check-overdue")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> checkOverdue() {
        int count = borrowService.checkOverdue();
        return Result.success("检测完成，发现 " + count + " 条逾期记录");
    }

    /**
     * 催还通知
     */
    @PostMapping("/admin/send-reminders")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> sendReminders() {
        int count = borrowService.sendReminders();
        return Result.success("已发送 " + count + " 条催还提醒");
    }

    // ==================== 读者自助操作 ====================

    /**
     * 读者预约借阅
     */
    @PostMapping("/reader/borrow")
    public Result<?> readerBorrow(@RequestBody Map<String, Long> params) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            BorrowRecord record = borrowService.borrowAvailableCopy(user.getUserId(), params.get("bookId"), null);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者预约借阅
     */
    @PostMapping("/reader/reserve")
    public Result<?> reserve(@RequestBody Map<String, Long> params) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            String msg = borrowService.reserve(user.getUserId(), params.get("bookId"));
            return Result.success(msg);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者取消预约
     */
    @DeleteMapping("/reader/reservations/{id}")
    public Result<?> cancelReservation(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            return borrowService.cancelReservationForReader(id, user.getUserId()) ? Result.success("取消成功") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者自助续借
     */
    @PostMapping("/reader/renew/{id}")
    public Result<?> readerRenew(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            String msg = borrowService.renewForReader(id, user.getUserId());
            return Result.success(msg);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 读者查看自己的借阅记录
     */
    @GetMapping("/reader/borrow-records")
    public Result<?> myBorrowRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        PageResult<BorrowRecord> result = borrowService.listBorrowRecords(page, size, user.getUserId(), status, null);
        return Result.success(result);
    }

    /**
     * 读者查看自己的预约记录
     */
    @GetMapping("/reader/reservations")
    public Result<?> myReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        return Result.success(borrowService.listReservations(page, size, user.getUserId(), null, null));
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
