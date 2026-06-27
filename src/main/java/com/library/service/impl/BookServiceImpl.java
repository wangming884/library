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
    private final BorrowRecordMapper borrowRecordMapper;

    public BookServiceImpl(BookMapper bookMapper, BookCopyMapper bookCopyMapper,
                            CategoryMapper categoryMapper, BookTagMapper bookTagMapper,
                            BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.categoryMapper = categoryMapper;
        this.bookTagMapper = bookTagMapper;
        this.borrowRecordMapper = borrowRecordMapper;
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
        fillBookSummary(result.getRecords());
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
        fillBookSummary(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Book getDetail(Long id) {
        Book book = bookMapper.selectWithCategory(id);
        if (book != null) {
            fillBookSummary(List.of(book));
        }
        return book;
    }

    @Override
    public boolean saveBook(Book book) {
        prepareBook(book, false);
        book.setTotalCount(0);
        book.setAvailableCount(0);
        return bookMapper.insert(book) > 0;
    }

    @Override
    public boolean updateBook(Book book) {
        prepareBook(book, true);
        book.setTotalCount(null);
        book.setAvailableCount(null);
        return bookMapper.updateById(book) > 0;
    }

    @Override
    @Transactional
    public boolean deleteBook(Long id) {
        long copyCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>().eq(BookCopy::getBookId, id));
        if (copyCount > 0) {
            throw new RuntimeException("该图书下存在副本，请先处理副本后再删除");
        }
        bookTagMapper.deleteRelationsByBookId(id);
        return bookMapper.deleteById(id) > 0;
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
        fillBookSummary(books);
        return books;
    }

    @Override
    public PageResult<BookCopy> listCopies(int page, int size, Long bookId, Integer status) {
        Page<BookCopy> result = bookCopyMapper.selectCopyPage(new Page<>(page, size), bookId, status);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public List<BookCopy> listAvailableCopies(Long bookId) {
        if (bookMapper.selectById(bookId) == null) {
            throw new RuntimeException("图书不存在");
        }
        reconcileBookInventory(bookId);
        return bookCopyMapper.selectList(new LambdaQueryWrapper<BookCopy>()
                .eq(BookCopy::getBookId, bookId)
                .eq(BookCopy::getStatus, Constants.COPY_STATUS_AVAILABLE)
                .orderByAsc(BookCopy::getLocation)
                .orderByAsc(BookCopy::getShelf)
                .orderByAsc(BookCopy::getId));
    }

    @Override
    @Transactional
    public boolean saveCopy(BookCopy copy) {
        if (copy.getBookId() == null) {
            throw new RuntimeException("请选择图书");
        }
        if (!StringUtils.hasText(copy.getBarcode())) {
            throw new RuntimeException("请输入条形码");
        }
        Book book = bookMapper.selectById(copy.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        BookCopy existing = bookCopyMapper.findByBarcode(copy.getBarcode().trim());
        if (existing != null) {
            throw new RuntimeException("条形码已存在，请更换后再提交");
        }
        copy.setBarcode(copy.getBarcode().trim());
        if (copy.getStatus() == null) {
            copy.setStatus(Constants.COPY_STATUS_AVAILABLE);
        }
        bookCopyMapper.insert(copy);
        refreshBookCounts(copy.getBookId());
        return true;
    }

    @Override
    public boolean updateCopyStatus(Long copyId, int status) {
        BookCopy copy = bookCopyMapper.selectById(copyId);
        if (copy == null) throw new RuntimeException("副本不存在");
        if (status < Constants.COPY_STATUS_AVAILABLE || status > Constants.COPY_STATUS_SEALED) {
            throw new RuntimeException("副本状态不合法");
        }
        int activeBorrowCount = borrowRecordMapper.countActiveByCopyId(copyId);
        if (activeBorrowCount > 0 && status == Constants.COPY_STATUS_AVAILABLE) {
            throw new RuntimeException("该副本存在未归还记录，不能手动改为在馆，请先办理归还");
        }
        if (activeBorrowCount == 0 && status == Constants.COPY_STATUS_BORROWED) {
            throw new RuntimeException("不能手动改为借出，请通过借书办理生成借阅记录");
        }
        copy.setStatus(status);
        bookCopyMapper.updateById(copy);

        refreshBookCounts(copy.getBookId());
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
        prepareCategory(category, false);
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(Category category) {
        prepareCategory(category, true);
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) throw new RuntimeException("存在子分类，无法删除");
        long bookCount = bookMapper.selectCount(new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, id));
        if (bookCount > 0) throw new RuntimeException("该分类下存在图书，无法删除");
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public List<BookTag> listTags() {
        return bookTagMapper.selectList(null);
    }

    @Override
    public boolean saveTag(BookTag tag) {
        if (!StringUtils.hasText(tag.getTagName())) {
            throw new RuntimeException("请输入标签名称");
        }
        tag.setTagName(tag.getTagName().trim());
        long duplicateCount = bookTagMapper.selectCount(new LambdaQueryWrapper<BookTag>()
                .eq(BookTag::getTagName, tag.getTagName()));
        if (duplicateCount > 0) {
            throw new RuntimeException("标签名称已存在");
        }
        return bookTagMapper.insert(tag) > 0;
    }

    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        bookTagMapper.deleteRelationsByTagId(id);
        return bookTagMapper.deleteById(id) > 0;
    }

    private void fillBookSummary(List<Book> books) {
        List<Category> categories = categoryMapper.selectList(null);
        for (Book book : books) {
            reconcileBookInventory(book.getId());
            if (book.getCategoryId() != null) {
                categories.stream().filter(c -> c.getId().equals(book.getCategoryId())).findFirst()
                        .ifPresent(c -> book.setCategoryName(c.getName()));
            }
            long totalCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                    .eq(BookCopy::getBookId, book.getId()));
            long availableCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                    .eq(BookCopy::getBookId, book.getId())
                    .eq(BookCopy::getStatus, Constants.COPY_STATUS_AVAILABLE));
            book.setTotalCount((int) totalCount);
            book.setAvailableCount((int) availableCount);
        }
    }

    private void reconcileBookInventory(Long bookId) {
        List<BookCopy> copies = bookCopyMapper.selectList(new LambdaQueryWrapper<BookCopy>()
                .eq(BookCopy::getBookId, bookId));
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
        refreshBookCounts(bookId);
    }

    private void refreshBookCounts(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            return;
        }
        long totalCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                .eq(BookCopy::getBookId, bookId));
        long availableCount = bookCopyMapper.selectCount(new LambdaQueryWrapper<BookCopy>()
                .eq(BookCopy::getBookId, bookId)
                .eq(BookCopy::getStatus, Constants.COPY_STATUS_AVAILABLE));
        book.setTotalCount((int) totalCount);
        book.setAvailableCount((int) availableCount);
        bookMapper.updateById(book);
    }

    private void prepareBook(Book book, boolean update) {
        if (!StringUtils.hasText(book.getTitle())) {
            throw new RuntimeException("请输入书名");
        }
        book.setTitle(book.getTitle().trim());
        if (StringUtils.hasText(book.getIsbn())) {
            book.setIsbn(book.getIsbn().trim());
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>().eq(Book::getIsbn, book.getIsbn());
            if (update && book.getId() != null) {
                wrapper.ne(Book::getId, book.getId());
            }
            if (bookMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("ISBN已存在，请更换后再提交");
            }
        }
        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category == null) {
                throw new RuntimeException("图书分类不存在");
            }
            book.setCategoryCode(category.getCode());
        }
        if (book.getIsRare() == null) {
            book.setIsRare(0);
        }
        if (book.getStatus() == null) {
            book.setStatus(1);
        }
    }

    private void prepareCategory(Category category, boolean update) {
        if (!StringUtils.hasText(category.getCode())) {
            throw new RuntimeException("请输入分类编码");
        }
        if (!StringUtils.hasText(category.getName())) {
            throw new RuntimeException("请输入分类名称");
        }
        category.setCode(category.getCode().trim());
        category.setName(category.getName().trim());
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getParentId() == 0) {
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new RuntimeException("上级分类不存在");
            }
            category.setLevel((parent.getLevel() == null ? 1 : parent.getLevel()) + 1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getCode, category.getCode());
        if (update && category.getId() != null) {
            wrapper.ne(Category::getId, category.getId());
        }
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("分类编码已存在，请更换后再提交");
        }
    }
}
