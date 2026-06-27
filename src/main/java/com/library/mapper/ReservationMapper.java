package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    @Select({
            "<script>",
            "SELECT rv.*, r.name AS reader_name, r.card_no AS reader_card_no, b.title AS book_title",
            "FROM reservation rv",
            "LEFT JOIN reader r ON rv.reader_id = r.id",
            "LEFT JOIN book b ON rv.book_id = b.id",
            "WHERE 1 = 1",
            "<if test='readerId != null'>AND rv.reader_id = #{readerId}</if>",
            "<if test='status != null'>AND rv.status = #{status}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (r.name LIKE CONCAT('%', #{keyword}, '%')",
            "OR r.card_no LIKE CONCAT('%', #{keyword}, '%')",
            "OR b.title LIKE CONCAT('%', #{keyword}, '%')",
            "OR b.isbn LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY rv.create_time DESC",
            "</script>"
    })
    Page<Reservation> selectReservationPage(Page<Reservation> page,
                                            @Param("readerId") Long readerId,
                                            @Param("status") Integer status,
                                            @Param("keyword") String keyword);
}
