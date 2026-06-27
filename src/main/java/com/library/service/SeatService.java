package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Seat;
import com.library.entity.SeatReservation;

import java.time.LocalDate;
import java.util.List;

public interface SeatService extends IService<Seat> {

    List<Seat> listByRoom(String roomName);

    boolean saveSeat(Seat seat);

    boolean reserve(Long readerId, Long seatId, LocalDate date, String startTime, String endTime);

    boolean checkIn(Long reservationId);

    boolean checkInForReader(Long reservationId, Long readerId);

    boolean release(Long reservationId);

    boolean releaseForReader(Long reservationId, Long readerId);

    PageResult<SeatReservation> listReservations(int page, int size, Long readerId, LocalDate date, Integer status);
}
