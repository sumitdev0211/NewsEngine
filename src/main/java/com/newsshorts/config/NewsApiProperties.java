package com.newsshorts.config;

import com.newsshorts.enums.Category;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * NewsAPI fetch settings — edit {@code application.properties} under {@code newsapi.category.*}
 * to change sources or search queries without touching Java code.
 */
@ConfigurationProperties(prefix = "newsapi")
public class NewsApiProperties {

    /** Max articles per request (NewsAPI limit is 100). */
    private int pageSize = 100;

    /**
     * Per-category fetch config. Keys match {@link Category} enum names in lowercase
     * (e.g. {@code general}, {@code entertainment}).
     */
    private Map<String, CategoryFetchConfig> category = new HashMap<>();

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, CategoryFetchConfig> getCategory() {
        return category;
    }

    public void setCategory(Map<String, CategoryFetchConfig> category) {
        this.category = category;
    }

    public CategoryFetchConfig configFor(Category category) {
        return this.category.getOrDefault(category.name().toLowerCase(), new CategoryFetchConfig());
    }

    /**
     * How to fetch articles for one app category.
     * Use {@code sources} OR {@code searchQuery} — sources takes priority if both are set.
     */
    public static class CategoryFetchConfig {

        /**
         * Comma-separated NewsAPI source IDs for GET /top-headlines?sources=...
         * Example: google-news-in,the-hindu,the-times-of-india
         */
        private String sources;

        /**
         * Search query for GET /everything?q=... when no dedicated sources exist.
         * Used for entertainment (no India entertainment sources on NewsAPI free tier).
         */
        private String searchQuery;

        public String getSources() {
            return sources;
        }

        public void setSources(String sources) {
            this.sources = sources;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public void setSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }
    }
}
