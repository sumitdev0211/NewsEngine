package com.newsshorts.dto.api;

public record FetchNowResponse(
        int categoriesProcessed,
        int totalFetched,
        int totalInserted,
        int totalSkipped
) {
}
