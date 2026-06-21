package com.newsshorts.dto.api;

import com.newsshorts.enums.Category;

import java.time.Instant;

public record FeedArticleResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        String sourceName,
        String sourceUrl,
        Category category,
        Instant publishedAt
) {
}
