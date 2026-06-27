package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.SeatReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface SeatReservationMapper extends BaseMapper<SeatReservation> {

    @Select({
            "<script>",
            "SELECT sr.*, r.name AS reader_name, s.seat_no AS seat_no, s.room_name AS room_name",
            "FROM seat_reservation sr",
            "LEFT JOIN reader r ON sr.reader_id = r.id",
            "LEFT JOIN seat s ON sr.seat_id = s.id",
            "WHERE 1 = 1",
            "<if test='readerId != null'>AND sr.reader_id = #{readerId}</if>",
            "<if test='date != null'>AND sr.reserve_date = #{date}</if>",
            "<if test='status != null'>AND sr.status = #{status}</if>",
            "ORDER BY sr.create_time DESC",
            "</script>"
    })
    Page<SeatReservation> selectReservationPage(Page<SeatReservation> page,
                                                @Param("readerId") Long readerId,
                                                @Param("date") LocalDate date,
                                                @Param("status") Integer status);
}
