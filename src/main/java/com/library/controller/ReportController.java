package com.library.controller;

import com.library.common.Result;
import com.library.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAnyRole('super_admin','cataloger','circulation','front_desk')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 借阅统计
     */
    @GetMapping("/borrow")
    public Result<?> borrowStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        return Result.success(reportService.borrowStatistics(year, month));
    }

    /**
     * 读者统计
     */
    @GetMapping("/reader")
    public Result<?> readerStatistics() {
        return Result.success(reportService.readerStatistics());
    }

    /**
     * 馆藏统计
     */
    @GetMapping("/collection")
    public Result<?> collectionStatistics() {
        return Result.success(reportService.collectionStatistics());
    }

    /**
     * 逾期统计
     */
    @GetMapping("/overdue")
    public Result<?> overdueStatistics() {
        return Result.success(reportService.overdueStatistics());
    }

    /**
     * 各院系借阅占比
     */
    @GetMapping("/dept-ratio")
    public Result<?> deptBorrowRatio() {
        return Result.success(reportService.deptBorrowRatio());
    }

    /**
     * 分类藏书数量
     */
    @GetMapping("/category-count")
    public Result<?> categoryBookCount() {
        return Result.success(reportService.categoryBookCount());
    }
}
