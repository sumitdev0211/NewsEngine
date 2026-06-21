package com.newsshorts.controller;

import com.newsshorts.dto.api.FetchNowResponse;
import com.newsshorts.service.NewsFetchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints for manual testing. Not intended for mobile clients.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final NewsFetchService newsFetchService;

    public AdminController(NewsFetchService newsFetchService) {
        this.newsFetchService = newsFetchService;
    }

    /**
     * Immediately fetches headlines from NewsAPI for all categories and saves new articles.
     * Skips duplicates. Returns counts of fetched, inserted, and skipped articles.
     */
    @PostMapping("/fetch-now")
    public FetchNowResponse fetchNow() {
        return newsFetchService.fetchAll();
    }
}
