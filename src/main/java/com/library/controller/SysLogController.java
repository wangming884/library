package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.SysLog;
import com.library.service.SysLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('super_admin')")
public class SysLogController {

    private final SysLogService logService;

    public SysLogController(SysLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public Result<?> listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        PageResult<SysLog> result = logService.listPage(page, size, keyword, status, operator, operation, startDate, endDate);
        return Result.success(result);
    }
}
