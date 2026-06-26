package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.FineRecord;
import com.library.security.LoginUser;
import com.library.service.FineService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 罚款财务管理控制器
 */
@RestController
@RequestMapping("/api")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    /**
     * 罚款记录列表
     */
    @GetMapping("/admin/fines")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> listFines(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        PageResult<FineRecord> result = fineService.listPage(page, size, readerId, status, type);
        return Result.success(result);
    }

    /**
     * 缴费
     */
    @PostMapping("/admin/fines/{id}/pay")
    @PreAuthorize("hasAnyRole('super_admin','circulation','front_desk')")
    public Result<?> payFine(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser user = getCurrentUser();
        try {
            return fineService.payFine(id, params.get("payMethod"), user != null ? user.getUserId() : null)
                    ? Result.success("缴费成功") : Result.error("缴费失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 财务统计
     */
    @GetMapping("/admin/fines/summary")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> financialSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) year = java.time.LocalDate.now().getYear();
        if (month == null) month = java.time.LocalDate.now().getMonthValue();
        return Result.success(fineService.getFinancialSummary(year, month));
    }

    /**
     * 欠费台账
     */
    @GetMapping("/admin/fines/debt-list")
    @PreAuthorize("hasAnyRole('super_admin','circulation')")
    public Result<?> debtList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(fineService.getDebtList(page, size));
    }

    /**
     * 读者查看自己的罚款记录
     */
    @GetMapping("/reader/fines")
    public Result<?> myFines(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        PageResult<FineRecord> result = fineService.listPage(page, size, user.getUserId(), null, null);
        return Result.success(result);
    }

    /**
     * 读者查看未缴金额
     */
    @GetMapping("/reader/fines/unpaid")
    public Result<?> myUnpaidAmount() {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        BigDecimal unpaid = fineService.getUnpaidAmount(user.getUserId());
        return Result.success(Map.of("unpaidAmount", unpaid));
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
