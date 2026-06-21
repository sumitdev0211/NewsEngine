package com.newsshorts.service;

import com.newsshorts.dto.api.ArticleDetailResponse;
import com.newsshorts.dto.api.FeedArticleResponse;
import com.newsshorts.dto.api.FeedPageResponse;
import com.newsshorts.entity.NewsArticle;
import com.newsshorts.enums.Category;
import com.newsshorts.repository.NewsArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Read-side service — queries stored articles and maps them to API responses.
 */
@Service
public class NewsService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final NewsArticleRepository repository;

    public NewsService(NewsArticleRepository repository) {
        this.repository = repository;
    }

    /**
     * Loads a page of articles sorted by publish date (newest first).
     * When category is null, returns articles across all categories.
     */
    public FeedPageResponse getFeed(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size));
        Page<NewsArticle> result = category == null
                ? repository.findAllByOrderByPublishedAtDesc(pageable)
                : repository.findByCategoryOrderByPublishedAtDesc(category, pageable);

        return toFeedPageResponse(result);
    }

    /**
     * Looks up a single article by ID. Returns empty if not found.
     */
    public Optional<ArticleDetailResponse> getArticle(Long id) {
        return repository.findById(id).map(this::toDetailResponse);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private FeedPageResponse toFeedPageResponse(Page<NewsArticle> page) {
        return new FeedPageResponse(
                page.getContent().stream().map(this::toFeedResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private FeedArticleResponse toFeedResponse(NewsArticle article) {
        return new FeedArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                article.getImageUrl(),
                article.getSourceName(),
                article.getSourceUrl(),
                article.getCategory(),
                article.getPublishedAt()
        );
    }

    private ArticleDetailResponse toDetailResponse(NewsArticle article) {
        // When content was redundant or missing at save time, fall back to description for detail view.
        String content = article.getContent() != null ? article.getContent() : article.getDescription();

        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                content,
                article.getImageUrl(),
                article.getSourceName(),
                article.getSourceUrl(),
                article.getCategory(),
                article.getPublishedAt(),
                article.getFetchedAt()
        );
    }
}
