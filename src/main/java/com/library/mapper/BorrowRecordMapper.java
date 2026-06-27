package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 查询读者当前借阅数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId} AND status IN (1, 3)")
    int countActiveBorrows(@Param("readerId") Long readerId);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE copy_id = #{copyId} AND status IN (1, 3)")
    int countActiveByCopyId(@Param("copyId") Long copyId);

    @Select({
            "<script>",
            "SELECT br.*, r.name AS reader_name, r.card_no AS reader_card_no,",
            "b.title AS book_title, b.isbn AS book_isbn, bc.barcode AS copy_barcode,",
            "bc.location AS copy_location, bc.floor AS copy_floor, bc.shelf AS copy_shelf",
            "FROM borrow_record br",
            "LEFT JOIN reader r ON br.reader_id = r.id",
            "LEFT JOIN book_copy bc ON br.copy_id = bc.id",
            "LEFT JOIN book b ON br.book_id = b.id",
            "WHERE 1 = 1",
            "<if test='readerId != null'>AND br.reader_id = #{readerId}</if>",
            "<if test='status != null'>AND br.status = #{status}</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (r.name LIKE CONCAT('%', #{keyword}, '%')",
            "OR r.card_no LIKE CONCAT('%', #{keyword}, '%')",
            "OR b.title LIKE CONCAT('%', #{keyword}, '%')",
            "OR b.isbn LIKE CONCAT('%', #{keyword}, '%')",
            "OR bc.barcode LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY br.create_time DESC",
            "</script>"
    })
    Page<BorrowRecord> selectRecordPage(Page<BorrowRecord> page,
                                        @Param("readerId") Long readerId,
                                        @Param("status") Integer status,
                                        @Param("keyword") String keyword);

    @Select("SELECT br.*, r.name AS reader_name, r.card_no AS reader_card_no, " +
            "b.title AS book_title, b.isbn AS book_isbn, bc.barcode AS copy_barcode, " +
            "bc.location AS copy_location, bc.floor AS copy_floor, bc.shelf AS copy_shelf " +
            "FROM borrow_record br " +
            "LEFT JOIN reader r ON br.reader_id = r.id " +
            "LEFT JOIN book_copy bc ON br.copy_id = bc.id " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "WHERE br.id = #{id}")
    BorrowRecord selectWithDetails(@Param("id") Long id);

    /**
     * 查询逾期记录（用于自动检测）
     */
    @Select("SELECT br.*, r.name as reader_name, r.phone, r.card_no, b.title as book_title " +
            "FROM borrow_record br " +
            "JOIN reader r ON br.reader_id = r.id " +
            "JOIN book_copy bc ON br.copy_id = bc.id " +
            "JOIN book b ON bc.book_id = b.id " +
            "WHERE br.status = 1 AND br.due_date < #{now}")
    List<Map<String, Object>> findOverdueRecords(@Param("now") LocalDateTime now);

    /**
     * 统计：日借阅量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE DATE(borrow_date) = #{date}")
    int countByDate(@Param("date") String date);

    /**
     * 统计：月借阅量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE YEAR(borrow_date) = #{year} AND MONTH(borrow_date) = #{month}")
    int countByMonth(@Param("year") int year, @Param("month") int month);
}
