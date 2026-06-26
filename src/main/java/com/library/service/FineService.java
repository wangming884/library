package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.FineRecord;

import java.math.BigDecimal;
import java.util.Map;

public interface FineService extends IService<FineRecord> {

    /**
     * 分页查询罚款记录
     */
    PageResult<FineRecord> listPage(int page, int size, Long readerId, Integer status, Integer type);

    /**
     * 缴费
     */
    boolean payFine(Long fineId, String payMethod, Long operatorId);

    /**
     * 获取读者未缴总额
     */
    BigDecimal getUnpaidAmount(Long readerId);

    /**
     * 财务统计
     */
    Map<String, Object> getFinancialSummary(Integer year, Integer month);

    /**
     * 欠费台账
     */
    PageResult<Map<String, Object>> getDebtList(int page, int size);
}
