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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        if (StringUtils.hasText(roomName)) wrapper.eq(Seat::getRoomName, roomName.trim());
        wrapper.orderByAsc(Seat::getRoomName).orderByAsc(Seat::getSeatNo);
        return seatMapper.selectList(wrapper);
    }

    @Override
    public boolean saveSeat(Seat seat) {
        prepareSeat(seat);
        return seatMapper.insert(seat) > 0;
    }

    @Override
    public boolean reserve(Long readerId, Long seatId, LocalDate date, String startTime, String endTime) {
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null || reader.getStatus() != 1) {
            throw new RuntimeException("读者状态异常，无法预约座位");
        }
        if (date == null) {
            throw new RuntimeException("请选择预约日期");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预约过去日期");
        }
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            throw new RuntimeException("请选择预约时间段");
        }

        // 检查座位是否可用
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null || seat.getStatus() != 1) {
            throw new RuntimeException("座位不可用");
        }

        // 检查时间冲突
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new RuntimeException("时间格式不正确，请使用 HH:mm");
        }
        if (!end.isAfter(start)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        if (date.equals(LocalDate.now()) && start.isBefore(LocalTime.now())) {
            throw new RuntimeException("不能预约已开始的时间段");
        }

        LambdaQueryWrapper<SeatReservation> conflict = new LambdaQueryWrapper<>();
        conflict.eq(SeatReservation::getSeatId, seatId)
                .eq(SeatReservation::getReserveDate, date)
                .in(SeatReservation::getStatus, 1, 2) // 预约中或已签到
                .lt(SeatReservation::getStartTime, end)
                .gt(SeatReservation::getEndTime, start);
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
    @Transactional
    public boolean checkIn(Long reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        return checkInReservation(reservation);
    }

    @Override
    @Transactional
    public boolean checkInForReader(Long reservationId, Long readerId) {
        if (readerId == null) {
            throw new RuntimeException("未登录");
        }
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!readerId.equals(reservation.getReaderId())) {
            throw new RuntimeException("只能签到自己的座位预约");
        }
        return checkInReservation(reservation);
    }

    @Override
    @Transactional
    public boolean release(Long reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        return releaseReservation(reservation);
    }

    @Override
    @Transactional
    public boolean releaseForReader(Long reservationId, Long readerId) {
        if (readerId == null) {
            throw new RuntimeException("未登录");
        }
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation == null) throw new RuntimeException("预约记录不存在");
        if (!readerId.equals(reservation.getReaderId())) {
            throw new RuntimeException("只能释放自己的座位预约");
        }
        return releaseReservation(reservation);
    }

    private boolean checkInReservation(SeatReservation reservation) {
        if (reservation.getStatus() != 1) {
            throw new RuntimeException("当前状态无法签到");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(reservation.getReserveDate(), reservation.getStartTime());
        LocalDateTime end = LocalDateTime.of(reservation.getReserveDate(), reservation.getEndTime());
        if (now.isBefore(start.minusMinutes(30))) {
            throw new RuntimeException("未到签到时间");
        }
        if (now.isAfter(end)) {
            reservation.setStatus(4);
            seatReservationMapper.updateById(reservation);
            throw new RuntimeException("预约已过期，无法签到");
        }
        reservation.setStatus(2);
        reservation.setCheckInTime(now);
        return seatReservationMapper.updateById(reservation) > 0;
    }

    private boolean releaseReservation(SeatReservation reservation) {
        if (reservation.getStatus() != 1 && reservation.getStatus() != 2) {
            throw new RuntimeException("当前状态无法释放");
        }
        reservation.setStatus(3);
        return seatReservationMapper.updateById(reservation) > 0;
    }

    @Override
    public PageResult<SeatReservation> listReservations(int page, int size, Long readerId, LocalDate date, Integer status) {
        Page<SeatReservation> result = seatReservationMapper.selectReservationPage(new Page<>(page, size), readerId, date, status);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    private void prepareSeat(Seat seat) {
        if (!StringUtils.hasText(seat.getRoomName())) {
            throw new RuntimeException("请输入自习室名称");
        }
        if (!StringUtils.hasText(seat.getSeatNo())) {
            throw new RuntimeException("请输入座位号");
        }
        seat.setRoomName(seat.getRoomName().trim());
        seat.setSeatNo(seat.getSeatNo().trim());
        if (StringUtils.hasText(seat.getFloor())) {
            seat.setFloor(seat.getFloor().trim());
        }
        if (seat.getStatus() == null) {
            seat.setStatus(1);
        }
        if (seat.getStatus() < 1 || seat.getStatus() > 3) {
            throw new RuntimeException("座位状态不合法");
        }
        long duplicateCount = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomName, seat.getRoomName())
                .eq(Seat::getSeatNo, seat.getSeatNo()));
        if (duplicateCount > 0) {
            throw new RuntimeException("该自习室下座位号已存在");
        }
    }
}
