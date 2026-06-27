package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.FineRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface FineRecordMapper extends BaseMapper<FineRecord> {

    @Select({
            "<script>",
            "SELECT fr.*, r.name AS reader_name, r.card_no AS reader_card_no,",
            "b.title AS book_title, a.real_name AS operator_name",
            "FROM fine_record fr",
            "LEFT JOIN reader r ON fr.reader_id = r.id",
            "LEFT JOIN borrow_record br ON fr.borrow_record_id = br.id",
            "LEFT JOIN book b ON br.book_id = b.id",
            "LEFT JOIN sys_admin a ON fr.operator_id = a.id",
            "WHERE 1 = 1",
            "<if test='readerId != null'>AND fr.reader_id = #{readerId}</if>",
            "<if test='status != null'>AND fr.status = #{status}</if>",
            "<if test='type != null'>AND fr.type = #{type}</if>",
            "ORDER BY fr.create_time DESC",
            "</script>"
    })
    Page<FineRecord> selectFinePage(Page<FineRecord> page,
                                    @Param("readerId") Long readerId,
                                    @Param("status") Integer status,
                                    @Param("type") Integer type);

    @Select("SELECT COALESCE(SUM(amount - paid_amount), 0) FROM fine_record WHERE reader_id = #{readerId} AND status = 0")
    BigDecimal getUnpaidAmount(@Param("readerId") Long readerId);

    @Select("SELECT COUNT(*) FROM fine_record WHERE borrow_record_id = #{borrowRecordId} AND type = #{type}")
    long countByBorrowRecordAndType(@Param("borrowRecordId") Long borrowRecordId, @Param("type") Integer type);

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM fine_record WHERE status = 1 AND DATE(pay_time) = #{date}")
    BigDecimal getDailyIncome(@Param("date") String date);

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM fine_record WHERE status = 1 AND YEAR(pay_time) = #{year} AND MONTH(pay_time) = #{month}")
    BigDecimal getMonthlyIncome(@Param("year") int year, @Param("month") int month);
}
