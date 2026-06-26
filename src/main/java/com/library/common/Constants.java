package com.library.common;

/**
 * 系统常量
 */
public class Constants {

    /** JWT token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";

    /** 角色标识 */
    public static final String ROLE_SUPER_ADMIN = "super_admin";
    public static final String ROLE_CATALOGER = "cataloger";
    public static final String ROLE_CIRCULATION = "circulation";
    public static final String ROLE_FRONT_DESK = "front_desk";

    /** 读者状态 */
    public static final int READER_STATUS_NORMAL = 1;
    public static final int READER_STATUS_LOST = 2;
    public static final int READER_STATUS_FROZEN = 3;
    public static final int READER_STATUS_BLACKLIST = 4;

    /** 副本状态 */
    public static final int COPY_STATUS_AVAILABLE = 1;
    public static final int COPY_STATUS_BORROWED = 2;
    public static final int COPY_STATUS_LOST = 3;
    public static final int COPY_STATUS_REPAIR = 4;
    public static final int COPY_STATUS_SEALED = 5;

    /** 借阅记录状态 */
    public static final int BORROW_STATUS_BORROWED = 1;
    public static final int BORROW_STATUS_RETURNED = 2;
    public static final int BORROW_STATUS_OVERDUE = 3;
    public static final int BORROW_STATUS_LOST = 4;

    /** 预约状态 */
    public static final int RESERVE_STATUS_WAITING = 1;
    public static final int RESERVE_STATUS_READY = 2;
    public static final int RESERVE_STATUS_CANCELLED = 3;
    public static final int RESERVE_STATUS_COMPLETED = 4;
    public static final int RESERVE_STATUS_EXPIRED = 5;

    /** 罚款类型 */
    public static final int FINE_TYPE_OVERDUE = 1;
    public static final int FINE_TYPE_DAMAGE = 2;
    public static final int FINE_TYPE_LOST = 3;
}
