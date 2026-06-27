package com.library.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer baseTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('book', 'reader')",
                Integer.class);
        if (baseTableCount == null || baseTableCount < 2) {
            return;
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS book_review (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    book_id BIGINT NOT NULL COMMENT '图书ID',
                    reader_id BIGINT NOT NULL COMMENT '读者ID',
                    rating TINYINT DEFAULT 5 COMMENT '评分 1-5',
                    content VARCHAR(500) NOT NULL COMMENT '评论内容',
                    status TINYINT DEFAULT 1 COMMENT '状态 1显示 0隐藏',
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_book_review_book (book_id),
                    INDEX idx_book_review_reader (reader_id),
                    FOREIGN KEY (book_id) REFERENCES book(id),
                    FOREIGN KEY (reader_id) REFERENCES reader(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书书评表'
                """);
    }
}
