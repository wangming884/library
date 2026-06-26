package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.SysConfig;
import com.library.mapper.SysConfigMapper;
import com.library.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final SysConfigMapper configMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public List<SysConfig> listAll() {
        return configMapper.selectList(null);
    }

    @Override
    public String getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public boolean batchUpdate(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SysConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, entry.getKey()));
            if (config != null) {
                config.setConfigValue(entry.getValue());
                configMapper.updateById(config);
            } else {
                config = new SysConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                configMapper.insert(config);
            }
        }
        return true;
    }
}
