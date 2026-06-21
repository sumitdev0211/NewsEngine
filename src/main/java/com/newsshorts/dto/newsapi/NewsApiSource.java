package com.newsshorts.dto.newsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsApiSource(
        String id,
        String name
) {
}
