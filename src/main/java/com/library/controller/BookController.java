package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookTag;
import com.library.entity.Category;
import com.library.service.BookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 图书管理控制器
 */
@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ==================== 图书检索（公开） ====================

    @GetMapping("/book/search")
    public Result<?> searchBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String author) {
        PageResult<Book> result = bookService.search(page, size, keyword, categoryId, publisher, author);
        return Result.success(result);
    }

    @GetMapping("/book/{id}")
    public Result<?> getBookDetail(@PathVariable Long id) {
        Book book = bookService.getDetail(id);
        if (book == null) return Result.error("图书不存在");
        return Result.success(book);
    }

    @GetMapping("/book/rank")
    public Result<?> getBorrowRank(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getBorrowRank(limit));
    }

    @GetMapping("/book/new")
    public Result<?> getNewBooks(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getNewBooks(limit));
    }

    // ==================== 管理员图书管理 ====================

    @GetMapping("/admin/books")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> listBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String isbn) {
        PageResult<Book> result = bookService.listPage(page, size, keyword, categoryId, isbn);
        return Result.success(result);
    }

    @PostMapping("/admin/books")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> addBook(@RequestBody Book book) {
        boolean success = bookService.save(book);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        return bookService.updateById(book) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteBook(@PathVariable Long id) {
        return bookService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ==================== 副本管理 ====================

    @GetMapping("/admin/copies")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> listCopies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status) {
        return Result.success(bookService.listCopies(page, size, bookId, status));
    }

    @PostMapping("/admin/copies")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> addCopy(@RequestBody BookCopy copy) {
        try {
            return bookService.saveCopy(copy) ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/copies/{id}/status")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> updateCopyStatus(@PathVariable Long id, @RequestParam int status) {
        try {
            return bookService.updateCopyStatus(id, status) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/copies/barcode/{barcode}")
    public Result<?> getCopyByBarcode(@PathVariable String barcode) {
        BookCopy copy = bookService.getCopyByBarcode(barcode);
        if (copy == null) return Result.error("未找到该条码对应的副本");
        return Result.success(copy);
    }

    // ==================== 分类管理 ====================

    @GetMapping("/categories")
    public Result<?> listCategories() {
        return Result.success(bookService.listCategories());
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> addCategory(@RequestBody Category category) {
        return bookService.saveCategory(category) ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        return bookService.updateCategory(category) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteCategory(@PathVariable Long id) {
        try {
            return bookService.deleteCategory(id) ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 标签管理 ====================

    @GetMapping("/admin/tags")
    public Result<?> listTags() {
        return Result.success(bookService.listTags());
    }

    @PostMapping("/admin/tags")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> addTag(@RequestBody BookTag tag) {
        return bookService.saveTag(tag) ? Result.success("添加成功") : Result.error("添加失败");
    }

    @DeleteMapping("/admin/tags/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteTag(@PathVariable Long id) {
        return bookService.deleteTag(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
