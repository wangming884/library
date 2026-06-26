package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Seat;
import com.library.entity.SeatReservation;
import com.library.entity.Reader;
import com.library.mapper.SeatMapper;
import com.library.mapper.SeatReservationMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.SeatService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    private final SeatMapper seatMapper;
    private final SeatReservationMapper seatReservationMapper;
    private final ReaderMapper readerMapper;

    public SeatServiceImpl(SeatMapper seatMapper, SeatReservationMapper seatReservationMapper,
                            ReaderMapper readerMapper) {
        this.seatMapper = seatMapper;
        this.seatReservationMapper = seatReservationMapper;
        this.readerMapper = readerMapper;
    }

    @Override
    public List<Seat> listByRoom(String roomName) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        if (roomName != null) wrapper.eq(Seat::getRoomName, roomName);
        return seatMapper.selectList(wrapper);
    }

    @Override
    public boolean saveSeat(Seat seat) {
        return seatMapper.insert(seat) > 0;
    }

    @Override
    public boolean reserve(Long readerId, Long seatId, LocalDate date, String startTime, String endTime) {
        // 检查座位是否可用
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || seat.getStatus() != 1) {
            throw new RuntimeException("座位不可用");
        }

        // 检查时间冲突
        LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));

        LambdaQueryWrapper<SeatReservation> conflict = new LambdaQueryWrapper<>();
        conflict.eq(SeatReservation::getSeatId, seatId)
                .eq(SeatReservation::getReserveDate, date)
                .in(SeatReservation::getStatus, 1, 2) // 预约中或已签到
                .and(w -> w
                        .and(w2 -> w2.lt(SeatReservation::getStartTime, end).gt(SeatReservation::getEndTime, start))
                );
        long count = seatReservationMapper.selectCount(conflict);
        if (count > 0) throw new RuntimeException("该时段座位已被预约");

        SeatReservation reservation = new SeatReservation();
        reservation.setReaderId(readerId);
        reservation.setSeatId(seatId);
        reservation.setReserveDate(date);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(1);
        return seatReservationMapper.insert(reservation) > 0;
    }

    @Override
    public boolean checkIn(Long reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null || reservation.getStatus() != 1) {
            throw new RuntimeException("预约记录无效");
        }
        reservation.setStatus(2);
        reservation.setCheckInTime(LocalDateTime.now());
        return seatReservationMapper.updateById(reservation) > 0;
    }

    @Override
    public boolean release(Long reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        reservation.setStatus(3);
        return seatReservationMapper.updateById(reservation) > 0;
    }

    @Override
    public PageResult<SeatReservation> listReservations(int page, int size, Long readerId, LocalDate date, Integer status) {
        LambdaQueryWrapper<SeatReservation> wrapper = new LambdaQueryWrapper<>();
        if (readerId != null) wrapper.eq(SeatReservation::getReaderId, readerId);
        if (date != null) wrapper.eq(SeatReservation::getReserveDate, date);
        if (status != null) wrapper.eq(SeatReservation::getStatus, status);
        wrapper.orderByDesc(SeatReservation::getCreateTime);

        Page<SeatReservation> result = seatReservationMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
}
