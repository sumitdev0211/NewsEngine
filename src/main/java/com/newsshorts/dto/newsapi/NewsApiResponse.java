package com.newsshorts.dto.newsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsApiResponse(
        String status,
        int totalResults,
        List<NewsApiArticle> articles
) {
}
