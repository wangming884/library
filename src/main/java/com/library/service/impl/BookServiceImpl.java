package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.Constants;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookTag;
import com.library.entity.Category;
import com.library.mapper.*;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;
    private final CategoryMapper categoryMapper;
    private final BookTagMapper bookTagMapper;

    public BookServiceImpl(BookMapper bookMapper, BookCopyMapper bookCopyMapper,
                            CategoryMapper categoryMapper, BookTagMapper bookTagMapper) {
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.categoryMapper = categoryMapper;
        this.bookTagMapper = bookTagMapper;
    }

    @Override
    public PageResult<Book> listPage(int page, int size, String keyword, Long categoryId, String isbn) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getKeywords, keyword));
        }
        if (categoryId != null) wrapper.eq(Book::getCategoryId, categoryId);
        if (StringUtils.hasText(isbn)) wrapper.eq(Book::getIsbn, isbn);
        wrapper.orderByDesc(Book::getCreateTime);

        Page<Book> result = bookMapper.selectPage(new Page<>(page, size), wrapper);
        fillCategoryName(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public PageResult<Book> search(int page, int size, String keyword, Long categoryId, String publisher, String author) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1); // 只搜上架的
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword)
                    .or().like(Book::getKeywords, keyword));
        }
        if (categoryId != null) wrapper.eq(Book::getCategoryId, categoryId);
        if (StringUtils.hasText(publisher)) wrapper.like(Book::getPublisher, publisher);
        if (StringUtils.hasText(author)) wrapper.like(Book::getAuthor, author);
        wrapper.orderByDesc(Book::getCreateTime);

        Page<Book> result = bookMapper.selectPage(new Page<>(page, size), wrapper);
        fillCategoryName(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Book getDetail(Long id) {
        return bookMapper.selectWithCategory(id);
    }

    @Override
    public List<Map<String, Object>> getBorrowRank(int limit) {
        return bookMapper.getBorrowRank(limit);
    }

    @Override
    public List<Book> getNewBooks(int limit) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1).orderByDesc(Book::getCreateTime).last("LIMIT " + limit);
        List<Book> books = bookMapper.selectList(wrapper);
        fillCategoryName(books);
        return books;
    }

    @Override
    public PageResult<BookCopy> listCopies(int page, int size, Long bookId, Integer status) {
        LambdaQueryWrapper<BookCopy> wrapper = new LambdaQueryWrapper<>();
        if (bookId != null) wrapper.eq(BookCopy::getBookId, bookId);
        if (status != null) wrapper.eq(BookCopy::getStatus, status);
        wrapper.orderByDesc(BookCopy::getCreateTime);

        Page<BookCopy> result = bookCopyMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public boolean saveCopy(BookCopy copy) {
        bookCopyMapper.insert(copy);
        // 更新图书副本数
        Book book = bookMapper.selectById(copy.getBookId());
        if (book != null) {
            book.setTotalCount(book.getTotalCount() + 1);
            book.setAvailableCount(book.getAvailableCount() + 1);
            bookMapper.updateById(book);
        }
        return true;
    }

    @Override
    public boolean updateCopyStatus(Long copyId, int status) {
        BookCopy copy = bookCopyMapper.selectById(copyId);
        if (copy == null) throw new RuntimeException("副本不存在");
        int oldStatus = copy.getStatus();
        copy.setStatus(status);
        bookCopyMapper.updateById(copy);

        // 更新图书可借数
        Book book = bookMapper.selectById(copy.getBookId());
        if (book != null) {
            if (oldStatus == Constants.COPY_STATUS_AVAILABLE && status != Constants.COPY_STATUS_AVAILABLE) {
                book.setAvailableCount(Math.max(0, book.getAvailableCount() - 1));
            } else if (oldStatus != Constants.COPY_STATUS_AVAILABLE && status == Constants.COPY_STATUS_AVAILABLE) {
                book.setAvailableCount(book.getAvailableCount() + 1);
            }
            if (status == Constants.COPY_STATUS_LOST) {
                book.setTotalCount(Math.max(0, book.getTotalCount() - 1));
            }
            bookMapper.updateById(book);
        }
        return true;
    }

    @Override
    public BookCopy getCopyByBarcode(String barcode) {
        return bookCopyMapper.findByBarcode(barcode);
    }

    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

    @Override
    public boolean saveCategory(Category category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(Category category) {
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) throw new RuntimeException("存在子分类，无法删除");
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public List<BookTag> listTags() {
        return bookTagMapper.selectList(null);
    }

    @Override
    public boolean saveTag(BookTag tag) {
        return bookTagMapper.insert(tag) > 0;
    }

    @Override
    public boolean deleteTag(Long id) {
        return bookTagMapper.deleteById(id) > 0;
    }

    private void fillCategoryName(List<Book> books) {
        List<Category> categories = categoryMapper.selectList(null);
        for (Book book : books) {
            if (book.getCategoryId() != null) {
                categories.stream().filter(c -> c.getId().equals(book.getCategoryId())).findFirst()
                        .ifPresent(c -> book.setCategoryName(c.getName()));
            }
        }
    }
}
