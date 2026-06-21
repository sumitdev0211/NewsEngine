package com.newsshorts.dto.api;

import com.newsshorts.enums.Category;

import java.time.Instant;

public record ArticleDetailResponse(
        Long id,
        String title,
        String description,
        String content,
        String imageUrl,
        String sourceName,
        String sourceUrl,
        Category category,
        Instant publishedAt,
        Instant fetchedAt
) {
}
