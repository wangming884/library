package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.SysLog;
import com.library.mapper.SysLogMapper;
import com.library.service.SysLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private final SysLogMapper logMapper;

    public SysLogServiceImpl(SysLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void log(Long adminId, String adminName, String operation, String method, String params, String ip, Long duration, int status) {
        SysLog log = new SysLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setDuration(duration);
        log.setStatus(status);
        logMapper.insert(log);
    }

    @Override
    public PageResult<SysLog> listPage(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysLog::getOperation, keyword).or().like(SysLog::getAdminName, keyword);
        }
        if (status != null) wrapper.eq(SysLog::getStatus, status);
        wrapper.orderByDesc(SysLog::getCreateTime);

        Page<SysLog> result = logMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
}
