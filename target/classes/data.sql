-- ==========================================
-- 图书馆管理系统 初始数据
-- ==========================================
USE library_db;

-- 初始角色
INSERT IGNORE INTO sys_role (id, role_name, role_key, description, permissions) VALUES
(1, '超级管理员', 'super_admin', '拥有所有权限', '*'),
(2, '图书采编员', 'cataloger', '图书编目和馆藏管理', 'book,book_copy,category'),
(3, '流通管理员', 'circulation', '借阅流通管理', 'borrow,reservation,fine,reader'),
(4, '前台操作员', 'front_desk', '日常借还操作', 'borrow,reservation,reader_query');

-- 初始管理员 (密码: admin123)
INSERT IGNORE INTO sys_admin (id, username, password, real_name, role_id, phone, status) VALUES
(1, 'admin', '$2a$10$y6yW/7HMJjKH2AfF2cAg4uksbu.oHkpVZhkxVMJa7cRch70mbdoNC', '系统管理员', 1, '13800000001', 1),
(2, 'cataloger', '$2a$10$y6yW/7HMJjKH2AfF2cAg4uksbu.oHkpVZhkxVMJa7cRch70mbdoNC', '张编辑', 2, '13800000002', 1),
(3, 'circulation', '$2a$10$y6yW/7HMJjKH2AfF2cAg4uksbu.oHkpVZhkxVMJa7cRch70mbdoNC', '李流通', 3, '13800000003', 1);

-- 读者类型
INSERT IGNORE INTO reader_type (id, type_name, type_key, max_borrow, max_days, renew_times, can_borrow_rare, description) VALUES
(1, '学生', 'student', 5, 30, 1, 0, '在校学生'),
(2, '教师', 'teacher', 10, 60, 3, 1, '教职工'),
(3, '校外人员', 'external', 3, 15, 0, 0, '社会读者');

-- 初始读者 (密码: reader123)
INSERT IGNORE INTO reader (id, card_no, name, type_id, gender, phone, email, dept, id_card, password, status, balance) VALUES
(1, 'R20240001', '王小明', 1, 1, '13900000001', 'wangxm@edu.cn', '计算机科学学院', '420000200101010001', '$2a$10$0yzfX4ho4.MdslisxoLuCe9sruKNNcO06kK3Ri4pf3AXs.5s0.t9a', 1, 0),
(2, 'R20240002', '李老师', 2, 2, '13900000002', 'lils@edu.cn', '信息管理学院', '420000198001010002', '$2a$10$0yzfX4ho4.MdslisxoLuCe9sruKNNcO06kK3Ri4pf3AXs.5s0.t9a', 1, 0),
(3, 'R20240003', '张三', 3, 1, '13900000003', 'zhangsan@qq.com', NULL, '420000199001010003', '$2a$10$0yzfX4ho4.MdslisxoLuCe9sruKNNcO06kK3Ri4pf3AXs.5s0.t9a', 1, 0);

-- 图书分类(中图法一级分类)
INSERT IGNORE INTO category (id, code, name, parent_id, level, sort) VALUES
(1, 'A', '马列主义、毛泽东思想、邓小平理论', 0, 1, 1),
(2, 'B', '哲学、宗教', 0, 1, 2),
(3, 'C', '社会科学总论', 0, 1, 3),
(4, 'D', '政治、法律', 0, 1, 4),
(5, 'E', '军事', 0, 1, 5),
(6, 'F', '经济', 0, 1, 6),
(7, 'G', '文化、科学、教育、体育', 0, 1, 7),
(8, 'H', '语言、文字', 0, 1, 8),
(9, 'I', '文学', 0, 1, 9),
(10, 'J', '艺术', 0, 1, 10),
(11, 'K', '历史、地理', 0, 1, 11),
(12, 'N', '自然科学总论', 0, 1, 12),
(13, 'O', '数理科学和化学', 0, 1, 13),
(14, 'P', '天文学、地球科学', 0, 1, 14),
(15, 'Q', '生物科学', 0, 1, 15),
(16, 'R', '医药、卫生', 0, 1, 16),
(17, 'S', '农业科学', 0, 1, 17),
(18, 'T', '工业技术', 0, 1, 18),
(19, 'TP', '自动化技术、计算机技术', 18, 2, 1),
(20, 'U', '交通运输', 0, 1, 20),
(21, 'V', '航空、航天', 0, 1, 21),
(22, 'X', '环境科学、安全科学', 0, 1, 22),
(23, 'Z', '综合性图书', 0, 1, 23);

-- 示例图书
INSERT IGNORE INTO book (id, isbn, title, author, publisher, pub_date, category_id, category_code, price, description, keywords, total_count, available_count, status) VALUES
(1, '978-7-115-54641-3', 'Java编程思想（第4版）', '[美] Bruce Eckel 著', '机械工业出版社', '2007-06-01', 19, 'TP', 108.00, 'Java领域经典著作，涵盖Java语言的各个方面。', 'Java,编程,面向对象', 3, 3, 1),
(2, '978-7-111-63666-0', 'Spring Boot实战', '丁雪丰 著', '机械工业出版社', '2020-01-01', 19, 'TP', 69.00, 'Spring Boot框架全面指南。', 'Spring Boot,微服务,Java', 2, 2, 1),
(3, '978-7-302-51760-8', '数据结构（C语言版）', '严蔚敏 著', '清华大学出版社', '2019-01-01', 19, 'TP', 49.00, '经典数据结构教材。', '数据结构,C语言,算法', 5, 5, 1),
(4, '978-7-02-000220-7', '红楼梦', '曹雪芹 著', '人民文学出版社', '1996-12-01', 9, 'I', 59.70, '中国古典四大名著之一。', '古典文学,四大名著,红楼梦', 3, 3, 1),
(5, '978-7-100-01166-8', '经济学原理（第7版）', '[美] 曼昆 著', '北京大学出版社', '2015-05-01', 6, 'F', 128.00, '世界最受欢迎的经济学入门教材。', '经济学,宏观,微观', 2, 2, 1);

-- 图书副本
INSERT IGNORE INTO book_copy (id, book_id, barcode, location, floor, shelf, status) VALUES
(1, 1, 'BK20240001', '计算机图书区', '3楼', 'A-01-01', 1),
(2, 1, 'BK20240002', '计算机图书区', '3楼', 'A-01-01', 1),
(3, 1, 'BK20240003', '计算机图书区', '3楼', 'A-01-01', 1),
(4, 2, 'BK20240004', '计算机图书区', '3楼', 'A-01-02', 1),
(5, 2, 'BK20240005', '计算机图书区', '3楼', 'A-01-02', 1),
(6, 3, 'BK20240006', '计算机图书区', '3楼', 'A-02-01', 1),
(7, 3, 'BK20240007', '计算机图书区', '3楼', 'A-02-01', 1),
(8, 3, 'BK20240008', '计算机图书区', '3楼', 'A-02-01', 1),
(9, 3, 'BK20240009', '计算机图书区', '3楼', 'A-02-01', 1),
(10, 3, 'BK20240010', '计算机图书区', '3楼', 'A-02-01', 1),
(11, 4, 'BK20240011', '文学图书区', '2楼', 'B-01-01', 1),
(12, 4, 'BK20240012', '文学图书区', '2楼', 'B-01-01', 1),
(13, 4, 'BK20240013', '文学图书区', '2楼', 'B-01-01', 1),
(14, 5, 'BK20240014', '社科图书区', '2楼', 'C-01-01', 1),
(15, 5, 'BK20240015', '社科图书区', '2楼', 'C-01-01', 1);

-- 系统配置
INSERT IGNORE INTO sys_config (config_key, config_value, description) VALUES
('borrow.max_count', '5', '单次最大借阅数量(默认)'),
('borrow.max_days', '30', '最大借阅天数(默认)'),
('borrow.renew_times', '1', '最大续借次数(默认)'),
('borrow.renew_days', '15', '每次续借天数'),
('fine.daily_rate', '0.50', '逾期每日罚款金额(元)'),
('fine.damage_rate', '0.50', '损毁赔偿比例(按书价%)'),
('reservation.expire_days', '3', '预约保留天数'),
('reservation.notify_days', '1', '到期前提醒天数'),
('system.name', '图书馆管理系统', '系统名称'),
('system.version', '1.0.0', '系统版本');

-- 图书标签
INSERT IGNORE INTO book_tag (id, tag_name) VALUES
(1, '经典'), (2, '教材'), (3, '畅销'), (4, '新书'), (5, '获奖');

INSERT IGNORE INTO book_tag_rel (book_id, tag_id) VALUES
(1, 1), (1, 2), (2, 3), (2, 4), (3, 2), (4, 1), (5, 2);

-- 座位（自习室）
INSERT IGNORE INTO seat (id, room_name, seat_no, floor, status) VALUES
(1, '第一自习室', 'A-001', '1楼', 1),
(2, '第一自习室', 'A-002', '1楼', 1),
(3, '第一自习室', 'A-003', '1楼', 1),
(4, '第一自习室', 'A-004', '1楼', 1),
(5, '第一自习室', 'A-005', '1楼', 1),
(6, '第二自习室', 'B-001', '2楼', 1),
(7, '第二自习室', 'B-002', '2楼', 1),
(8, '第二自习室', 'B-003', '2楼', 1),
(9, '第二自习室', 'B-004', '2楼', 1),
(10, '第二自习室', 'B-005', '2楼', 1);

-- 公告
INSERT IGNORE INTO announcement (id, title, content, type, admin_id) VALUES
(1, '图书馆开馆时间通知', '图书馆开馆时间：周一至周五 8:00-22:00，周六日 9:00-18:00。', 1, 1),
(2, '新书上架公告', '本月新到计算机类图书50余册，欢迎借阅！', 1, 1);
