package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Seat;
import com.library.entity.SeatReservation;
import com.library.security.LoginUser;
import com.library.service.SeatService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 座位预约控制器
 */
@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * 查询可用座位
     */
    @GetMapping("/seat/available")
    public Result<?> availableSeats(@RequestParam(required = false) String roomName) {
        List<Seat> seats = seatService.listByRoom(roomName);
        return Result.success(seats);
    }

    /**
     * 预约座位
     */
    @PostMapping("/reader/seat/reserve")
    public Result<?> reserveSeat(@RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            Long seatId = Long.valueOf(params.get("seatId").toString());
            LocalDate date = LocalDate.parse(params.get("date").toString());
            String startTime = params.get("startTime").toString();
            String endTime = params.get("endTime").toString();
            seatService.reserve(user.getUserId(), seatId, date, startTime, endTime);
            return Result.success("预约成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 签到
     */
    @PostMapping("/seat/checkin/{id}")
    public Result<?> checkIn(@PathVariable Long id) {
        try {
            return seatService.checkIn(id) ? Result.success("签到成功") : Result.error("签到失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 释放座位
     */
    @PostMapping("/seat/release/{id}")
    public Result<?> release(@PathVariable Long id) {
        try {
            return seatService.release(id) ? Result.success("已释放") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 座位预约记录
     */
    @GetMapping("/admin/seat-reservations")
    @PreAuthorize("hasAnyRole('super_admin','front_desk')")
    public Result<?> listSeatReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer status) {
        return Result.success(seatService.listReservations(page, size, readerId, date, status));
    }

    /**
     * 管理员管理座位
     */
    @PostMapping("/admin/seats")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> addSeat(@RequestBody Seat seat) {
        return seatService.saveSeat(seat) ? Result.success("添加成功") : Result.error("添加失败");
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
