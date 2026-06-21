package com.newsshorts.controller;

import com.newsshorts.dto.api.ArticleDetailResponse;
import com.newsshorts.dto.api.CategoriesResponse;
import com.newsshorts.dto.api.FeedPageResponse;
import com.newsshorts.enums.Category;
import com.newsshorts.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public read API for mobile clients — serves stored articles from the database.
 */
@RestController
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * Returns a paginated list of articles, newest first.
     * Optionally filter by category (e.g. ?category=GENERAL).
     */
    @GetMapping("/feed")
    public FeedPageResponse getFeed(
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return newsService.getFeed(category, page, size);
    }

    /**
     * Returns full detail for a single article by its database ID.
     * Responds with 404 if the article does not exist.
     */
    @GetMapping("/feed/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticle(@PathVariable Long id) {
        return newsService.getArticle(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns all supported news categories (e.g. GENERAL, ENTERTAINMENT).
     */
    @GetMapping("/categories")
    public CategoriesResponse getCategories() {
        return CategoriesResponse.all();
    }
}
