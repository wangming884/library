package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.FineRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface FineRecordMapper extends BaseMapper<FineRecord> {

    @Select("SELECT COALESCE(SUM(amount - paid_amount), 0) FROM fine_record WHERE reader_id = #{readerId} AND status = 0")
    BigDecimal getUnpaidAmount(@Param("readerId") Long readerId);

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM fine_record WHERE status = 1 AND DATE(pay_time) = #{date}")
    BigDecimal getDailyIncome(@Param("date") String date);

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM fine_record WHERE status = 1 AND YEAR(pay_time) = #{year} AND MONTH(pay_time) = #{month}")
    BigDecimal getMonthlyIncome(@Param("year") int year, @Param("month") int month);
}
