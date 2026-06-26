package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.SysConfig;

import java.util.List;
import java.util.Map;

public interface SysConfigService extends IService<SysConfig> {

    /**
     * 获取所有配置
     */
    List<SysConfig> listAll();

    /**
     * 获取配置值
     */
    String getValue(String key);

    /**
     * 获取配置值（带默认值）
     */
    String getValue(String key, String defaultValue);

    /**
     * 批量更新配置
     */
    boolean batchUpdate(Map<String, String> configs);
}
