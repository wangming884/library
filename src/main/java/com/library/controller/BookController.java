package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookTag;
import com.library.entity.Category;
import com.library.service.BookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @GetMapping("/book/{id}/available-copies")
    public Result<?> listAvailableCopies(@PathVariable Long id) {
        try {
            return Result.success(bookService.listAvailableCopies(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
        try {
            return bookService.saveBook(book) ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        try {
            return bookService.updateBook(book) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admin/books/cover")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("请选择图片文件");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过 5MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            String originalFilename = file.getOriginalFilename();
            if (StringUtils.hasText(originalFilename)
                    && (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\"))) {
                return Result.error("文件名不合法");
            }
            String ext = StringUtils.getFilenameExtension(originalFilename);
            if (!StringUtils.hasText(ext)) {
                return Result.error("图片文件缺少扩展名");
            }
            ext = ext.toLowerCase();
            Set<String> allowed = Set.of("jpg", "jpeg", "png", "gif", "webp");
            if (!allowed.contains(ext)) {
                return Result.error("仅支持 jpg、jpeg、png、gif、webp 格式");
            }

            Path uploadDir = Paths.get("uploads", "covers").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                return Result.error("文件路径不合法");
            }
            file.transferTo(target);

            return Result.success(Map.of("url", "/library/uploads/covers/" + filename));
        } catch (IOException e) {
            return Result.error("封面上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteBook(@PathVariable Long id) {
        try {
            return bookService.deleteBook(id) ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 副本管理 ====================

    @GetMapping("/admin/copies")
    @PreAuthorize("hasAnyRole('super_admin','cataloger','circulation','front_desk')")
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
    @PreAuthorize("hasAnyRole('super_admin','cataloger','circulation','front_desk')")
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
        try {
            return bookService.saveCategory(category) ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAnyRole('super_admin','cataloger')")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        try {
            return bookService.updateCategory(category) ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
        try {
            return bookService.saveTag(tag) ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/admin/tags/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<?> deleteTag(@PathVariable Long id) {
        try {
            return bookService.deleteTag(id) ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
