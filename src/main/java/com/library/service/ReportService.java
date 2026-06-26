package com.library.service;

import java.util.List;
import java.util.Map;

public interface ReportService {

    /**
     * 借阅统计
     */
    Map<String, Object> borrowStatistics(Integer year, Integer month);

    /**
     * 读者统计
     */
    Map<String, Object> readerStatistics();

    /**
     * 馆藏统计
     */
    Map<String, Object> collectionStatistics();

    /**
     * 逾期统计
     */
    Map<String, Object> overdueStatistics();

    /**
     * 各院系借阅占比
     */
    List<Map<String, Object>> deptBorrowRatio();

    /**
     * 分类藏书数量
     */
    List<Map<String, Object>> categoryBookCount();
}
