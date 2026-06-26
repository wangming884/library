package com.library.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.library.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat")
public class Seat extends BaseEntity {
    private String roomName;
    private String seatNo;
    private String floor;
    private Integer status;
}
