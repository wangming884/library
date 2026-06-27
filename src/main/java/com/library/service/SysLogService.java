package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.SysLog;

import java.time.LocalDate;

public interface SysLogService extends IService<SysLog> {

    /**
     * 记录日志
     */
    void log(Long adminId, String adminName, String operation, String method, String params, String ip, Long duration, int status);

    /**
     * 分页查询日志
     */
    PageResult<SysLog> listPage(int page, int size, String keyword, Integer status);

    /**
     * 分页查询日志（带细分筛选）
     */
    PageResult<SysLog> listPage(int page, int size, String keyword, Integer status,
                                String operator, String operation, LocalDate startDate, LocalDate endDate);
}
