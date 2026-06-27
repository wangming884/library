package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BookReview;

public interface BookReviewService extends IService<BookReview> {
    PageResult<BookReview> listByBook(int page, int size, Long bookId);

    BookReview addReview(Long bookId, Long readerId, BookReview review);
}
