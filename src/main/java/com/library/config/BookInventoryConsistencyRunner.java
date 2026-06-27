package com.library.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.Constants;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.mapper.BookCopyMapper;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reconciles legacy or manually edited inventory data with active borrow records.
 */
@Component
public class BookInventoryConsistencyRunner implements ApplicationRunner {

    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public BookInventoryConsistencyRunner(BookMapper bookMapper,
                                          BookCopyMapper bookCopyMapper,
                                          BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        reconcileCopyStatuses();
        refreshAllBookCounts();
    }

    private void reconcileCopyStatuses() {
        List<BookCopy> copies = bookCopyMapper.selectList(null);
        for (BookCopy copy : copies) {
            int activeBorrowCount = borrowRecordMapper.countActiveByCopyId(copy.getId());
            Integer currentStatus = copy.getStatus();
            Integer expectedStatus = currentStatus;

            if (activeBorrowCount > 0) {
                expectedStatus = Constants.COPY_STATUS_BORROWED;
            } else if (currentStatus == null
                    || currentStatus < Constants.COPY_STATUS_AVAILABLE
                    || currentStatus > Constants.COPY_STATUS_SEALED
                    || currentStatus == Constants.COPY_STATUS_BORROWED) {
                expectedStatus = Constants.COPY_STATUS_AVAILABLE;
            }

            if (!expectedStatus.equals(currentStatus)) {
                copy.setStatus(expectedStatus);
                bookCopyMapper.updateById(copy);
            }
        }
    }

    private void refreshAllBookCounts() {
        List<Book> books = bookMapper.selectList(null);
        for (Book book : books) {
            long totalCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                    .eq(BookCopy::getBookId, book.getId()));
            long availableCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                    .eq(BookCopy::getBookId, book.getId())
                    .eq(BookCopy::getStatus, Constants.COPY_STATUS_AVAILABLE));
            if (!Integer.valueOf((int) totalCount).equals(book.getTotalCount())
                    || !Integer.valueOf((int) availableCount).equals(book.getAvailableCount())) {
                book.setTotalCount((int) totalCount);
                book.setAvailableCount((int) availableCount);
                bookMapper.updateById(book);
            }
        }
    }
}
