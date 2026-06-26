package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.Constants;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final ReaderMapper readerMapper;
    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;
    private final CategoryMapper categoryMapper;
    private final ReaderTypeMapper readerTypeMapper;
    private final FineRecordMapper fineRecordMapper;

    public ReportServiceImpl(BorrowRecordMapper borrowRecordMapper, ReaderMapper readerMapper,
                              BookMapper bookMapper, BookCopyMapper bookCopyMapper,
                              CategoryMapper categoryMapper, ReaderTypeMapper readerTypeMapper,
                              FineRecordMapper fineRecordMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.readerMapper = readerMapper;
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.categoryMapper = categoryMapper;
        this.readerTypeMapper = readerTypeMapper;
        this.fineRecordMapper = fineRecordMapper;
    }

    @Override
    public Map<String, Object> borrowStatistics(Integer year, Integer month) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 今日借阅量
        stats.put("todayBorrows", borrowRecordMapper.countByDate(today.toString()));
        // 本月借阅量
        int y = year != null ? year : today.getYear();
        int m = month != null ? month : today.getMonthValue();
        stats.put("monthBorrows", borrowRecordMapper.countByMonth(y, m));
        // 总借阅量
        stats.put("totalBorrows", borrowRecordMapper.selectCount(null));
        // 当前在借数量
        stats.put("activeBorrows", borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, Constants.BORROW_STATUS_BORROWED)));
        // 逾期数量
        stats.put("overdueCount", borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, Constants.BORROW_STATUS_OVERDUE)));

        // 近7天每天借阅量
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("count", borrowRecordMapper.countByDate(date.toString()));
            dailyTrend.add(day);
        }
        stats.put("dailyTrend", dailyTrend);

        return stats;
    }

    @Override
    public Map<String, Object> readerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReaders", readerMapper.selectCount(null));
        stats.put("activeReaders", readerMapper.selectCount(
                new LambdaQueryWrapper<Reader>().eq(Reader::getStatus, Constants.READER_STATUS_NORMAL)));
        stats.put("blacklistCount", readerMapper.selectCount(
                new LambdaQueryWrapper<Reader>().eq(Reader::getStatus, Constants.READER_STATUS_BLACKLIST)));
        stats.put("frozenCount", readerMapper.selectCount(
                new LambdaQueryWrapper<Reader>().eq(Reader::getStatus, Constants.READER_STATUS_FROZEN)));

        // 各类型读者数量
        List<ReaderType> types = readerTypeMapper.selectList(null);
        List<Map<String, Object>> typeStats = new ArrayList<>();
        for (ReaderType type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("typeName", type.getTypeName());
            item.put("count", readerMapper.selectCount(
                    new LambdaQueryWrapper<Reader>().eq(Reader::getTypeId, type.getId())));
            typeStats.add(item);
        }
        stats.put("typeStats", typeStats);

        return stats;
    }

    @Override
    public Map<String, Object> collectionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookMapper.selectCount(null));
        stats.put("totalCopies", bookCopyMapper.selectCount(null));
        stats.put("availableCopies", bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getStatus, Constants.COPY_STATUS_AVAILABLE)));
        stats.put("borrowedCopies", bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getStatus, Constants.COPY_STATUS_BORROWED)));
        stats.put("lostCopies", bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getStatus, Constants.COPY_STATUS_LOST)));
        stats.put("repairCopies", bookCopyMapper.selectCount(
                new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getStatus, Constants.COPY_STATUS_REPAIR)));
        stats.put("totalCategories", categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1)));

        return stats;
    }

    @Override
    public Map<String, Object> overdueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("overdueCount", borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, Constants.BORROW_STATUS_OVERDUE)));

        // 逾期罚金总额
        BigDecimal totalOverdueFine = fineRecordMapper.selectList(
                new LambdaQueryWrapper<FineRecord>().eq(FineRecord::getType, Constants.FINE_TYPE_OVERDUE))
                .stream().map(FineRecord::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.put("totalOverdueFine", totalOverdueFine);

        // 未缴逾期罚金
        BigDecimal unpaidOverdue = fineRecordMapper.selectList(
                new LambdaQueryWrapper<FineRecord>()
                        .eq(FineRecord::getType, Constants.FINE_TYPE_OVERDUE)
                        .eq(FineRecord::getStatus, 0))
                .stream().map(f -> f.getAmount().subtract(f.getPaidAmount())).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        stats.put("unpaidOverdueFine", unpaidOverdue);

        return stats;
    }

    @Override
    public List<Map<String, Object>> deptBorrowRatio() {
        // 按院系统计借阅次数
        List<Reader> readers = readerMapper.selectList(null);
        Map<String, Long> deptCounts = new HashMap<>();
        for (Reader reader : readers) {
            String dept = reader.getDept() != null ? reader.getDept() : "未知";
            long count = borrowRecordMapper.selectCount(
                    new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getReaderId, reader.getId()));
            deptCounts.merge(dept, (long) count, Long::sum);
        }
        return deptCounts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("dept", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> categoryBookCount() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getLevel, 1));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", cat.getName());
            item.put("categoryCode", cat.getCode());
            item.put("bookCount", bookMapper.selectCount(
                    new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, cat.getId())));
            result.add(item);
        }
        return result;
    }
}
