package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.SysConfig;
import com.library.mapper.SysConfigMapper;
import com.library.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final SysConfigMapper configMapper;
    private static final Set<String> INTEGER_KEYS = Set.of(
            "borrow.max_count",
            "borrow.max_days",
            "borrow.renew_times",
            "borrow.renew_days",
            "reservation.expire_days",
            "reservation.notify_days"
    );
    private static final Set<String> DECIMAL_KEYS = Set.of(
            "fine.daily_rate",
            "fine.damage_rate"
    );

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
            String key = entry.getKey();
            String value = normalizeAndValidate(key, entry.getValue());
            SysConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (config != null) {
                config.setConfigValue(value);
                configMapper.updateById(config);
            } else {
                config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                configMapper.insert(config);
            }
        }
        return true;
    }

    private String normalizeAndValidate(String key, String value) {
        if (!StringUtils.hasText(key)) {
            throw new RuntimeException("配置键不能为空");
        }
        if (value == null) {
            throw new RuntimeException("配置值不能为空: " + key);
        }
        value = value.trim();
        if (INTEGER_KEYS.contains(key)) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    throw new RuntimeException(configName(key) + "不能小于0");
                }
                if (!"borrow.renew_times".equals(key) && parsed == 0) {
                    throw new RuntimeException(configName(key) + "必须大于0");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException(configName(key) + "必须是整数");
            }
        }
        if (DECIMAL_KEYS.contains(key)) {
            try {
                java.math.BigDecimal parsed = new java.math.BigDecimal(value);
                if (parsed.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    throw new RuntimeException(configName(key) + "不能小于0");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException(configName(key) + "必须是数字");
            }
        }
        return value;
    }

    private String configName(String key) {
        return switch (key) {
            case "borrow.max_count" -> "最大借阅数量";
            case "borrow.max_days" -> "最大借阅天数";
            case "borrow.renew_times" -> "最大续借次数";
            case "borrow.renew_days" -> "续借天数";
            case "reservation.expire_days" -> "预约保留天数";
            case "reservation.notify_days" -> "预约提醒天数";
            case "fine.daily_rate" -> "逾期每日罚款";
            case "fine.damage_rate" -> "损毁赔偿比例";
            default -> key;
        };
    }
}
