package com.newsshorts.enums;

/**
 * Supported news categories. Single source of truth for the app.
 * <p>
 * NewsAPI fetch settings (sources / search queries) for each category live in
 * {@code application.properties} under {@code newsapi.category.*}.
 */
public enum Category {
    GENERAL,
    ENTERTAINMENT
}
