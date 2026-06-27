package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;

public interface BorrowService extends IService<BorrowRecord> {

     /**
     * 借书
     */
    BorrowRecord borrow(Long readerId, Long copyId, Long operatorId);

     /**
     * 按图书借书，自动选择一个在馆副本
     */
    BorrowRecord borrowAvailableCopy(Long readerId, Long bookId, Long operatorId);

     /**
     * 还书
     */
    BorrowRecord returnBook(Long borrowRecordId, Long operatorId);

    /**
     * 续借
     */
    String renew(Long borrowRecordId, Long operatorId);

    /**
     * 读者续借自己的借阅记录
     */
    String renewForReader(Long borrowRecordId, Long readerId);

    /**
     * 预约
 */
    String reserve(Long readerId, Long bookId);

    /**
     * 取消预约
     */
    boolean cancelReservation(Long reservationId);

    /**
     * 读者取消自己的预约
     */
    boolean cancelReservationForReader(Long reservationId, Long readerId);

    /**
     * 分页查询借阅记录
     */
    PageResult<BorrowRecord> listBorrowRecords(int page, int size, Long readerId, Integer status, String keyword);

    /**
     * 分页查询预约记录
     */
    PageResult<Reservation> listReservations(int page, int size, Long readerId, Integer status, String keyword);

    /**
     * 检测并处理逾期（定时任务或手动触发）
     */
    int checkOverdue();

    /**
     * 读者当前借阅数
     */
    int countActiveBorrows(Long readerId);

    /**
     * 催还通知（站内信/标记）
     */
    int sendReminders();
}
