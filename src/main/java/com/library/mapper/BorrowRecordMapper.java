package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 查询读者当前借阅数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId} AND status = 1")
    int countActiveBorrows(@Param("readerId") Long readerId);

    /**
     * 查询逾期记录（用于自动检测）
     */
    @Select("SELECT br.*, r.name as reader_name, r.phone, r.card_no, b.title as book_title " +
            "FROM borrow_record br " +
            "JOIN reader r ON br.reader_id = r.id " +
            "JOIN book_copy bc ON br.copy_id = bc.id " +
            "JOIN book b ON bc.book_id = b.id " +
            "WHERE br.status = 1 AND br.due_date < #{now}")
    List<Map<String, Object>> findOverdueRecords(@Param("now") LocalDateTime now);

    /**
     * 统计：日借阅量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE DATE(borrow_date) = #{date}")
    int countByDate(@Param("date") String date);

    /**
     * 统计：月借阅量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE YEAR(borrow_date) = #{year} AND MONTH(borrow_date) = #{month}")
    int countByMonth(@Param("year") int year, @Param("month") int month);
}
