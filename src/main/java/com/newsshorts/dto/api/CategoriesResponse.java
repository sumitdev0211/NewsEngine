package com.newsshorts.dto.api;

import com.newsshorts.enums.Category;

import java.util.Arrays;
import java.util.List;

public record CategoriesResponse(List<Category> categories) {

    public static CategoriesResponse all() {
        return new CategoriesResponse(Arrays.asList(Category.values()));
    }
}
