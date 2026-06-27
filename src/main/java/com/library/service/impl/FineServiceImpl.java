package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.FineRecord;
import com.library.entity.Reader;
import com.library.mapper.FineRecordMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.FineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FineServiceImpl extends ServiceImpl<FineRecordMapper, FineRecord> implements FineService {

    private final FineRecordMapper fineRecordMapper;
    private final ReaderMapper readerMapper;

    public FineServiceImpl(FineRecordMapper fineRecordMapper, ReaderMapper readerMapper) {
        this.fineRecordMapper = fineRecordMapper;
        this.readerMapper = readerMapper;
    }

    @Override
    public PageResult<FineRecord> listPage(int page, int size, Long readerId, Integer status, Integer type) {
        Page<FineRecord> result = fineRecordMapper.selectFinePage(new Page<>(page, size), readerId, status, type);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public boolean payFine(Long fineId, String payMethod, Long operatorId) {
        FineRecord fine = fineRecordMapper.selectById(fineId);
        if (fine == null) throw new RuntimeException("罚款记录不存在");
        if (fine.getStatus() == 1) throw new RuntimeException("该罚款已缴纳");
        if (!StringUtils.hasText(payMethod)) {
            throw new RuntimeException("请选择缴费方式");
        }

        BigDecimal amount = fine.getAmount() == null ? BigDecimal.ZERO : fine.getAmount();
        BigDecimal paidAmount = fine.getPaidAmount() == null ? BigDecimal.ZERO : fine.getPaidAmount();
        BigDecimal unpaid = amount.subtract(paidAmount);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
            fine.setStatus(1);
            fine.setPaidAmount(amount);
            fineRecordMapper.updateById(fine);
            return true;
        }
        fine.setPaidAmount(amount);
        fine.setStatus(1);
        fine.setPayTime(LocalDateTime.now());
        fine.setPayMethod(payMethod.trim());
        fine.setOperatorId(operatorId);
        fineRecordMapper.updateById(fine);

        // 更新读者余额（减少欠费）
        Reader reader = readerMapper.selectById(fine.getReaderId());
        if (reader != null) {
            BigDecimal balance = reader.getBalance() == null ? BigDecimal.ZERO : reader.getBalance();
            reader.setBalance(balance.add(unpaid));
            readerMapper.updateById(reader);
        }
        return true;
    }

    @Override
    public BigDecimal getUnpaidAmount(Long readerId) {
        return fineRecordMapper.getUnpaidAmount(readerId);
    }

    @Override
    public Map<String, Object> getFinancialSummary(Integer year, Integer month) {
        Map<String, Object> summary = new HashMap<>();
        String today = java.time.LocalDate.now().toString();

        summary.put("dailyIncome", fineRecordMapper.getDailyIncome(today));
        summary.put("monthlyIncome", fineRecordMapper.getMonthlyIncome(year, month));

        // 总罚款金额
        BigDecimal totalFines = fineRecordMapper.selectList(
                new LambdaQueryWrapper<FineRecord>().eq(FineRecord::getStatus, 1))
                .stream().map(this::safePaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalIncome", totalFines);

        // 未缴总额
        BigDecimal totalUnpaid = fineRecordMapper.selectList(
                new LambdaQueryWrapper<FineRecord>().eq(FineRecord::getStatus, 0))
                .stream().map(this::unpaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalUnpaid", totalUnpaid);

        return summary;
    }

    @Override
    public PageResult<Map<String, Object>> getDebtList(int page, int size) {
        // 查询所有有未缴罚款的读者
        Page<Map<String, Object>> pageObj = new Page<>(page, size);
        // 使用自定义方式：遍历所有有欠费的读者
        List<FineRecord> unpaidFines = fineRecordMapper.selectList(
                new LambdaQueryWrapper<FineRecord>().eq(FineRecord::getStatus, 0));
        Map<Long, BigDecimal> readerDebts = new HashMap<>();
        for (FineRecord f : unpaidFines) {
            if (f.getReaderId() != null) {
                readerDebts.merge(f.getReaderId(), unpaidAmount(f), BigDecimal::add);
            }
        }

        List<Map<String, Object>> debtList = new java.util.ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : readerDebts.entrySet()) {
            Reader reader = readerMapper.selectById(entry.getKey());
            if (reader != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("readerId", reader.getId());
                item.put("readerName", reader.getName());
                item.put("cardNo", reader.getCardNo());
                item.put("phone", reader.getPhone());
                item.put("debtAmount", entry.getValue());
                debtList.add(item);
            }
        }

        // 手动分页
        int total = debtList.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<Map<String, Object>> subList = fromIndex < total ? debtList.subList(fromIndex, toIndex) : List.of();

        return new PageResult<>(subList, total, page, size);
    }

    private BigDecimal safeAmount(FineRecord fine) {
        return fine.getAmount() == null ? BigDecimal.ZERO : fine.getAmount();
    }

    private BigDecimal safePaidAmount(FineRecord fine) {
        return fine.getPaidAmount() == null ? BigDecimal.ZERO : fine.getPaidAmount();
    }

    private BigDecimal unpaidAmount(FineRecord fine) {
        BigDecimal unpaid = safeAmount(fine).subtract(safePaidAmount(fine));
        return unpaid.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : unpaid;
    }
}
