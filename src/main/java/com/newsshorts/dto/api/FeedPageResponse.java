package com.newsshorts.dto.api;

import java.util.List;

public record FeedPageResponse(
        List<FeedArticleResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
