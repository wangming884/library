package com.library.controller;

import com.library.common.Result;
import com.library.entity.SysConfig;
import com.library.service.SysConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('super_admin')")
public class SysConfigController {

    private final SysConfigService configService;

    public SysConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public Result<?> listConfigs() {
        List<SysConfig> configs = configService.listAll();
        return Result.success(configs);
    }

    @PutMapping
    public Result<?> updateConfigs(@RequestBody Map<String, String> configs) {
        try {
            configService.batchUpdate(configs);
            return Result.success("配置更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{key}")
    public Result<?> getConfig(@PathVariable String key) {
        String value = configService.getValue(key);
        if (value == null) return Result.error("配置不存在");
        return Result.success(Map.of("key", key, "value", value));
    }
}
