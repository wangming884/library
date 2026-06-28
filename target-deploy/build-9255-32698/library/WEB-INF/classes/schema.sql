-- ==========================================
-- 图书馆管理系统 数据库建表脚本
-- ==========================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE library_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------
-- 1. 系统管理员表
-- ------------------------------------------
DROP TABLE IF EXISTS sys_log;
DROP TABLE IF EXISTS sys_admin;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(200) COMMENT '描述',
    permissions TEXT COMMENT '权限列表(JSON)',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role_id BIGINT COMMENT '角色ID',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    last_login DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB COMMENT='系统管理员表';

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT COMMENT '操作人ID',
    admin_name VARCHAR(50) COMMENT '操作人姓名',
    operation VARCHAR(200) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration BIGINT COMMENT '耗时(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态 1成功 0失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='操作日志表';

-- ------------------------------------------
-- 2. 读者管理表
-- ------------------------------------------
DROP TABLE IF EXISTS fine_record;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book_review;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS seat_reservation;
DROP TABLE IF EXISTS reader;

CREATE TABLE IF NOT EXISTS reader_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
    type_key VARCHAR(50) NOT NULL UNIQUE COMMENT '类型标识',
    max_borrow INT DEFAULT 5 COMMENT '最大借阅数量',
    max_days INT DEFAULT 30 COMMENT '最大借阅天数',
    renew_times INT DEFAULT 1 COMMENT '最大续借次数',
    can_borrow_rare TINYINT DEFAULT 0 COMMENT '是否可借稀有书籍 1是 0否',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='读者类型表';

CREATE TABLE IF NOT EXISTS reader (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_no VARCHAR(30) NOT NULL UNIQUE COMMENT '读者证号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    type_id BIGINT COMMENT '读者类型ID',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    dept VARCHAR(100) COMMENT '院系/单位',
    id_card VARCHAR(30) COMMENT '证件号',
    photo VARCHAR(500) COMMENT '照片URL',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 2挂失 3冻结 4黑名单',
    balance DECIMAL(10,2) DEFAULT 0 COMMENT '账户余额(负数为欠费)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES reader_type(id)
) ENGINE=InnoDB COMMENT='读者表';

-- ------------------------------------------
-- 3. 图书管理表
-- ------------------------------------------
DROP TABLE IF EXISTS book_copy;
DROP TABLE IF EXISTS book_tag_rel;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS book_tag;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL COMMENT '分类号',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='图书分类表';

CREATE TABLE IF NOT EXISTS book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(30) COMMENT 'ISBN',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(200) COMMENT '作者',
    publisher VARCHAR(200) COMMENT '出版社',
    pub_date DATE COMMENT '出版日期',
    category_id BIGINT COMMENT '分类ID',
    category_code VARCHAR(30) COMMENT '分类号',
    price DECIMAL(10,2) COMMENT '价格',
    description TEXT COMMENT '简介',
    cover VARCHAR(500) COMMENT '封面URL',
    keywords VARCHAR(500) COMMENT '关键词',
    total_count INT DEFAULT 0 COMMENT '总副本数',
    available_count INT DEFAULT 0 COMMENT '可借副本数',
    is_rare TINYINT DEFAULT 0 COMMENT '是否稀有 1是 0否',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='图书表';

CREATE TABLE IF NOT EXISTS book_copy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    barcode VARCHAR(50) NOT NULL UNIQUE COMMENT '条码号',
    location VARCHAR(100) COMMENT '存放位置',
    floor VARCHAR(20) COMMENT '楼层',
    shelf VARCHAR(50) COMMENT '书架编号',
    status TINYINT DEFAULT 1 COMMENT '状态 1在馆 2借出 3丢失 4维修 5封存',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES book(id)
) ENGINE=InnoDB COMMENT='图书副本表';

CREATE TABLE IF NOT EXISTS book_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='图书标签表';

CREATE TABLE IF NOT EXISTS book_tag_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_book_tag (book_id, tag_id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (tag_id) REFERENCES book_tag(id)
) ENGINE=InnoDB COMMENT='图书标签关联表';

-- ------------------------------------------
-- 4. 借阅流通表
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    copy_id BIGINT NOT NULL COMMENT '副本ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    borrow_date DATETIME NOT NULL COMMENT '借书日期',
    due_date DATETIME NOT NULL COMMENT '应还日期',
    return_date DATETIME COMMENT '实际归还日期',
    renew_count INT DEFAULT 0 COMMENT '已续借次数',
    status TINYINT DEFAULT 1 COMMENT '状态 1借出 2已还 3逾期 4丢失',
    operator_id BIGINT COMMENT '办理人ID',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (copy_id) REFERENCES book_copy(id),
    FOREIGN KEY (book_id) REFERENCES book(id)
) ENGINE=InnoDB COMMENT='借阅记录表';

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reserve_date DATETIME NOT NULL COMMENT '预约日期',
    expire_date DATETIME COMMENT '保留到期日',
    notify_time DATETIME COMMENT '通知时间',
    status TINYINT DEFAULT 1 COMMENT '状态 1等待 2就绪 3取消 4完成 5过期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id)
) ENGINE=InnoDB COMMENT='预约记录表';

CREATE TABLE IF NOT EXISTS book_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL COMMENT '图书ID',
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    rating TINYINT DEFAULT 5 COMMENT '评分 1-5',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态 1显示 0隐藏',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
) ENGINE=InnoDB COMMENT='图书书评表';

-- ------------------------------------------
-- 5. 罚款财务表
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS fine_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    borrow_record_id BIGINT COMMENT '关联借阅记录',
    type TINYINT NOT NULL COMMENT '类型 1逾期 2损毁 3丢失',
    amount DECIMAL(10,2) NOT NULL COMMENT '应缴金额',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已缴金额',
    status TINYINT DEFAULT 0 COMMENT '状态 0未缴 1已缴',
    pay_time DATETIME COMMENT '缴费时间',
    pay_method VARCHAR(20) COMMENT '缴费方式',
    operator_id BIGINT COMMENT '经办人',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
) ENGINE=InnoDB COMMENT='罚款记录表';

-- ------------------------------------------
-- 6. 系统配置表
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统配置表';

-- ------------------------------------------
-- 7. 增值功能表
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(100) NOT NULL COMMENT '自习室名称',
    seat_no VARCHAR(30) NOT NULL COMMENT '座位号',
    floor VARCHAR(20) COMMENT '楼层',
    status TINYINT DEFAULT 1 COMMENT '状态 1空闲 2占用 3维护',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='座位表';

CREATE TABLE IF NOT EXISTS seat_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT NOT NULL COMMENT '读者ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    reserve_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    check_in_time DATETIME COMMENT '签到时间',
    status TINYINT DEFAULT 1 COMMENT '状态 1预约中 2已签到 3已释放 4未签到(违约)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
) ENGINE=InnoDB COMMENT='座位预约表';

CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type TINYINT DEFAULT 1 COMMENT '类型 1通知 2活动 3闭馆',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    admin_id BIGINT COMMENT '发布人ID',
    publish_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '状态 1发布 0草稿',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='公告表';

CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_id BIGINT COMMENT '读者ID',
    type TINYINT DEFAULT 1 COMMENT '类型 1购书建议 2图书丢失 3投诉 4其他',
    title VARCHAR(200) COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    reply TEXT COMMENT '回复内容',
    admin_id BIGINT COMMENT '回复人ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0未回复 1已回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    reply_time DATETIME COMMENT '回复时间',
    FOREIGN KEY (reader_id) REFERENCES reader(id)
) ENGINE=InnoDB COMMENT='留言反馈表';

-- ------------------------------------------
-- 索引优化
-- ------------------------------------------
CREATE INDEX idx_reader_card_no ON reader(card_no);
CREATE INDEX idx_reader_name ON reader(name);
CREATE INDEX idx_reader_phone ON reader(phone);
CREATE INDEX idx_book_isbn ON book(isbn);
CREATE INDEX idx_book_title ON book(title);
CREATE INDEX idx_book_author ON book(author);
CREATE INDEX idx_book_copy_barcode ON book_copy(barcode);
CREATE INDEX idx_borrow_reader ON borrow_record(reader_id);
CREATE INDEX idx_borrow_status ON borrow_record(status);
CREATE INDEX idx_borrow_due_date ON borrow_record(due_date);
CREATE INDEX idx_reservation_reader ON reservation(reader_id);
CREATE INDEX idx_book_review_book ON book_review(book_id);
CREATE INDEX idx_book_review_reader ON book_review(reader_id);
CREATE INDEX idx_fine_reader ON fine_record(reader_id);
CREATE INDEX idx_fine_status ON fine_record(status);
CREATE INDEX idx_sys_log_time ON sys_log(create_time);

SET FOREIGN_KEY_CHECKS = 1;
