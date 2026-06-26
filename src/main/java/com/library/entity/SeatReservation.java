package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_reservation")
public class SeatReservation extends BaseEntity {
    private Long readerId;
    private Long seatId;
    private LocalDate reserveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
    private Integer status;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String readerName;
    @TableField(exist = false)
    private String seatNo;
    @TableField(exist = false)
    private String roomName;
}
