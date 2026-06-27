package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.entity.BookReview;
import com.library.entity.Reader;
import com.library.mapper.BookMapper;
import com.library.mapper.BookReviewMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.BookReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BookReviewServiceImpl extends ServiceImpl<BookReviewMapper, BookReview> implements BookReviewService {

    private final BookReviewMapper bookReviewMapper;
    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;

    public BookReviewServiceImpl(BookReviewMapper bookReviewMapper,
                                 BookMapper bookMapper,
                                 ReaderMapper readerMapper) {
        this.bookReviewMapper = bookReviewMapper;
        this.bookMapper = bookMapper;
        this.readerMapper = readerMapper;
    }

    @Override
    public PageResult<BookReview> listByBook(int page, int size, Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("请选择图书");
        }
        Page<BookReview> result = bookReviewMapper.selectReviewPage(new Page<>(page, size), bookId);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public BookReview addReview(Long bookId, Long readerId, BookReview review) {
        if (bookId == null) {
            throw new RuntimeException("请选择图书");
        }
        if (readerId == null) {
            throw new RuntimeException("未登录");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }

        if (review == null) {
            throw new RuntimeException("请输入评论内容");
        }

        String content = review.getContent();
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("请输入评论内容");
        }
        content = content.trim();
        if (content.length() > 500) {
            throw new RuntimeException("评论内容不能超过500字");
        }

        Integer rating = review.getRating();
        if (rating == null) {
            rating = 5;
        }
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分范围为1到5分");
        }

        BookReview entity = new BookReview();
        entity.setBookId(bookId);
        entity.setReaderId(readerId);
        entity.setContent(content);
        entity.setRating(rating);
        entity.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        bookReviewMapper.insert(entity);

        BookReview detail = bookReviewMapper.selectWithDetails(entity.getId());
        return detail != null ? detail : entity;
    }
}
