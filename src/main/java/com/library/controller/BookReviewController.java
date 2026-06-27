package com.library.controller;

import com.library.common.Result;
import com.library.entity.BookReview;
import com.library.security.LoginUser;
import com.library.service.BookReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    public BookReviewController(BookReviewService bookReviewService) {
        this.bookReviewService = bookReviewService;
    }

    @GetMapping("/book/{bookId}/reviews")
    public Result<?> listReviews(@PathVariable Long bookId,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(bookReviewService.listByBook(page, size, bookId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reader/book/{bookId}/reviews")
    public Result<?> addReview(@PathVariable Long bookId, @RequestBody BookReview review) {
        LoginUser user = getCurrentUser();
        if (user == null) return Result.error(401, "未登录");
        try {
            return Result.success(bookReviewService.addReview(bookId, user.getUserId(), review));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }
}
