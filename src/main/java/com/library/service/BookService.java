package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.Category;
import com.library.entity.BookTag;

import java.util.List;
import java.util.Map;

public interface BookService extends IService<Book> {

    /**
     * 分页查询图书
     */
    PageResult<Book> listPage(int page, int size, String keyword, Long categoryId, String isbn);

    /**
     * 搜索图书（读者前台）
     */
    PageResult<Book> search(int page, int size, String keyword, Long categoryId, String publisher, String author);

    /**
     * 获取图书详情（含分类信息）
     */
    Book getDetail(Long id);

    /**
     * 借阅排行榜
     */
    List<Map<String, Object>> getBorrowRank(int limit);

    /**
     * 新书上架
     */
    List<Book> getNewBooks(int limit);

    // ---- 副本管理 ----
    PageResult<BookCopy> listCopies(int page, int size, Long bookId, Integer status);

    boolean saveCopy(BookCopy copy);

    boolean updateCopyStatus(Long copyId, int status);

    BookCopy getCopyByBarcode(String barcode);

    // ---- 分类管理 ----
    List<Category> listCategories();

    boolean saveCategory(Category category);

    boolean updateCategory(Category category);

    boolean deleteCategory(Long id);

    // ---- 标签管理 ----
    List<BookTag> listTags();

    boolean saveTag(BookTag tag);

    boolean deleteTag(Long id);
}
