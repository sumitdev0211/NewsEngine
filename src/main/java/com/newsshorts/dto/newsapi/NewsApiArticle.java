package com.newsshorts.dto.newsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsApiArticle(
        String title,
        String description,
        String content,
        String url,
        String urlToImage,
        String publishedAt,
        NewsApiSource source
) {
}
