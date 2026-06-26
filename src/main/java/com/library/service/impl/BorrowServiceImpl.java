package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.Constants;
import com.library.common.PageResult;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BorrowService;
import com.library.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final ReaderMapper readerMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BookMapper bookMapper;
    private final ReservationMapper reservationMapper;
    private final FineRecordMapper fineRecordMapper;
    private final SysConfigService configService;

    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper, ReaderMapper readerMapper,
                              BookCopyMapper bookCopyMapper, BookMapper bookMapper,
                              ReservationMapper reservationMapper, FineRecordMapper fineRecordMapper,
                              SysConfigService configService) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.readerMapper = readerMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.bookMapper = bookMapper;
        this.reservationMapper = reservationMapper;
        this.fineRecordMapper = fineRecordMapper;
        this.configService = configService;
    }

    @Override
    @Transactional
    public String borrow(Long readerId, Long copyId, Long operatorId) {
        // 校验读者
        Reader reader = readerMapper.selectWithTypeInfo(readerId);
        if (reader == null) throw new RuntimeException("读者不存在");
        if (reader.getStatus() == Constants.READER_STATUS_BLACKLIST) {
            throw new RuntimeException("该读者在黑名单中，无法借书");
        }
        if (reader.getStatus() != Constants.READER_STATUS_NORMAL) {
            throw new RuntimeException("读者状态异常，无法借书");
        }

        // 校验是否有欠费
        BigDecimal unpaid = fineRecordMapper.getUnpaidAmount(readerId);
        if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("读者有未缴罚款 ¥" + unpaid + "，请先缴费");
        }

        // 校验借阅数量
        int maxBorrow = reader.getMaxBorrow() != null ? reader.getMaxBorrow()
                : Integer.parseInt(configService.getValue("borrow.max_count", "5"));
        int activeBorrows = borrowRecordMapper.countActiveBorrows(readerId);
        if (activeBorrows >= maxBorrow) {
            throw new RuntimeException("已达最大借阅数量 " + maxBorrow);
        }

        // 校验副本
        BookCopy copy = bookCopyMapper.selectById(copyId);
        if (copy == null) throw new RuntimeException("图书副本不存在");
        if (copy.getStatus() != Constants.COPY_STATUS_AVAILABLE) {
            throw new RuntimeException("该副本不在馆，无法借出");
        }

        // 校验是否为稀有书籍
        Book book = bookMapper.selectById(copy.getBookId());
        if (book != null && book.getIsRare() != null && book.getIsRare() == 1) {
            Integer canBorrowRare = reader.getRenewTimes(); // 复用字段获取readerType
            // 需要从reader_type获取
            if (canBorrowRare == null || canBorrowRare == 0) {
                // 重新查询 reader_type
            }
        }

        // 创建借阅记录
        int borrowDays = reader.getMaxDays() != null ? reader.getMaxDays()
                : Integer.parseInt(configService.getValue("borrow.max_days", "30"));
        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setReaderId(readerId);
        record.setCopyId(copyId);
        record.setBookId(copy.getBookId());
        record.setBorrowDate(now);
        record.setDueDate(now.plusDays(borrowDays));
        record.setRenewCount(0);
        record.setStatus(Constants.BORROW_STATUS_BORROWED);
        record.setOperatorId(operatorId);
        borrowRecordMapper.insert(record);

        // 更新副本状态
        copy.setStatus(Constants.COPY_STATUS_BORROWED);
        bookCopyMapper.updateById(copy);

        // 更新图书可借数
        book.setAvailableCount(Math.max(0, book.getAvailableCount() - 1));
        bookMapper.updateById(book);

        return "借阅成功，应还日期：" + record.getDueDate().toLocalDate();
    }

    @Override
    @Transactional
    public String returnBook(Long borrowRecordId, Long operatorId) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowRecordId);
        if (record == null) throw new RuntimeException("借阅记录不存在");
        if (record.getStatus() != Constants.BORROW_STATUS_BORROWED
                && record.getStatus() != Constants.BORROW_STATUS_OVERDUE) {
            throw new RuntimeException("该记录状态不允许归还");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReturnDate(now);
        record.setOperatorId(operatorId);

        StringBuilder msg = new StringBuilder("归还成功");

        // 检查是否逾期
        if (now.isAfter(record.getDueDate())) {
            record.setStatus(Constants.BORROW_STATUS_OVERDUE);
            // 计算罚金
            long overdueDays = java.time.Duration.between(record.getDueDate(), now).toDays();
            if (overdueDays == 0) overdueDays = 1;
            BigDecimal dailyRate = new BigDecimal(configService.getValue("fine.daily_rate", "0.50"));
            BigDecimal fineAmount = dailyRate.multiply(BigDecimal.valueOf(overdueDays));

            FineRecord fine = new FineRecord();
            fine.setReaderId(record.getReaderId());
            fine.setBorrowRecordId(record.getId());
            fine.setType(Constants.FINE_TYPE_OVERDUE);
            fine.setAmount(fineAmount);
            fine.setPaidAmount(BigDecimal.ZERO);
            fine.setStatus(0);
            fineRecordMapper.insert(fine);
            msg.append("，逾期 ").append(overdueDays).append(" 天，罚款 ¥").append(fineAmount);
        } else {
            record.setStatus(Constants.BORROW_STATUS_RETURNED);
        }
        borrowRecordMapper.updateById(record);

        // 恢复副本状态
        BookCopy copy = bookCopyMapper.selectById(record.getCopyId());
        if (copy != null) {
            copy.setStatus(Constants.COPY_STATUS_AVAILABLE);
            bookCopyMapper.updateById(copy);

            // 更新图书可借数
            Book book = bookMapper.selectById(copy.getBookId());
            if (book != null) {
                book.setAvailableCount(book.getAvailableCount() + 1);
                bookMapper.updateById(book);
            }
        }

        // 检查是否有预约等待
        LambdaQueryWrapper<Reservation> rw = new LambdaQueryWrapper<>();
        rw.eq(Reservation::getBookId, record.getBookId())
          .eq(Reservation::getStatus, Constants.RESERVE_STATUS_WAITING)
          .orderByAsc(Reservation::getCreateTime)
          .last("LIMIT 1");
        Reservation reservation = reservationMapper.selectOne(rw);
        if (reservation != null) {
            int reserveDays = Integer.parseInt(configService.getValue("reservation.expire_days", "3"));
            reservation.setStatus(Constants.RESERVE_STATUS_READY);
            reservation.setExpireDate(now.plusDays(reserveDays));
            reservation.setNotifyTime(now);
            reservationMapper.updateById(reservation);
            msg.append("；有读者预约，已通知取书");
        }

        return msg.toString();
    }

    @Override
    @Transactional
    public String renew(Long borrowRecordId, Long operatorId) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowRecordId);
        if (record == null) throw new RuntimeException("借阅记录不存在");
        if (record.getStatus() != Constants.BORROW_STATUS_BORROWED) {
            throw new RuntimeException("当前状态不允许续借");
        }

        Reader reader = readerMapper.selectWithTypeInfo(record.getReaderId());
        int maxRenew = reader != null && reader.getRenewTimes() != null ? reader.getRenewTimes()
                : Integer.parseInt(configService.getValue("borrow.renew_times", "1"));

        if (record.getRenewCount() >= maxRenew) {
            throw new RuntimeException("已达最大续借次数 " + maxRenew);
        }

        // 检查是否已逾期
        if (LocalDateTime.now().isAfter(record.getDueDate())) {
            throw new RuntimeException("已逾期，无法续借，请先归还");
        }

        int renewDays = Integer.parseInt(configService.getValue("borrow.renew_days", "15"));
        record.setDueDate(record.getDueDate().plusDays(renewDays));
        record.setRenewCount(record.getRenewCount() + 1);
        if (operatorId != null) record.setOperatorId(operatorId);
        borrowRecordMapper.updateById(record);

        return "续借成功，新应还日期：" + record.getDueDate().toLocalDate();
    }

    @Override
    @Transactional
    public String reserve(Long readerId, Long bookId) {
        // 校验读者
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null || reader.getStatus() != Constants.READER_STATUS_NORMAL) {
            throw new RuntimeException("读者状态异常");
        }

        // 检查是否还有可借副本
        Book book = bookMapper.selectById(bookId);
        if (book == null) throw new RuntimeException("图书不存在");
        if (book.getAvailableCount() > 0) {
            throw new RuntimeException("该图书有可借副本，请直接借阅");
        }

        // 检查是否已预约
        LambdaQueryWrapper<Reservation> qw = new LambdaQueryWrapper<>();
        qw.eq(Reservation::getReaderId, readerId)
          .eq(Reservation::getBookId, bookId)
          .in(Reservation::getStatus, Constants.RESERVE_STATUS_WAITING, Constants.RESERVE_STATUS_READY);
        long count = reservationMapper.selectCount(qw);
        if (count > 0) throw new RuntimeException("您已预约该图书，请勿重复预约");

        Reservation reservation = new Reservation();
        reservation.setReaderId(readerId);
        reservation.setBookId(bookId);
        reservation.setReserveDate(LocalDateTime.now());
        reservation.setStatus(Constants.RESERVE_STATUS_WAITING);
        reservationMapper.insert(reservation);

        return "预约成功，图书归还后将通知您";
    }

    @Override
    public boolean cancelReservation(Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        if (reservation.getStatus() != Constants.RESERVE_STATUS_WAITING) {
            throw new RuntimeException("当前状态无法取消");
        }
        reservation.setStatus(Constants.RESERVE_STATUS_CANCELLED);
        return reservationMapper.updateById(reservation) > 0;
    }

    @Override
    public PageResult<BorrowRecord> listBorrowRecords(int page, int size, Long readerId, Integer status, String keyword) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        if (readerId != null) wrapper.eq(BorrowRecord::getReaderId, readerId);
        if (status != null) wrapper.eq(BorrowRecord::getStatus, status);
        wrapper.orderByDesc(BorrowRecord::getCreateTime);

        Page<BorrowRecord> result = borrowRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public PageResult<Reservation> listReservations(int page, int size, Long readerId, Integer status) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (readerId != null) wrapper.eq(Reservation::getReaderId, readerId);
        if (status != null) wrapper.eq(Reservation::getStatus, status);
        wrapper.orderByDesc(Reservation::getCreateTime);

        Page<Reservation> result = reservationMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public int checkOverdue() {
        // 将所有逾期未还的记录状态标记为逾期
        List<BorrowRecord> overdueRecords = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, Constants.BORROW_STATUS_BORROWED)
                        .lt(BorrowRecord::getDueDate, LocalDateTime.now()));
        int count = 0;
        for (BorrowRecord record : overdueRecords) {
            record.setStatus(Constants.BORROW_STATUS_OVERDUE);
            borrowRecordMapper.updateById(record);
            count++;
        }
        return count;
    }

    @Override
    public int countActiveBorrows(Long readerId) {
        return borrowRecordMapper.countActiveBorrows(readerId);
    }

    @Override
    public int sendReminders() {
        // 查找即将到期的借阅记录（1天内到期）
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        List<BorrowRecord> records = borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getStatus, Constants.BORROW_STATUS_BORROWED)
                        .lt(BorrowRecord::getDueDate, tomorrow));
        // 实际项目中这里发送短信/站内信，此处仅返回数量
        return records.size();
    }
}
