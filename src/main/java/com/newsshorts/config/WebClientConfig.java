package com.newsshorts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Configures the HTTP client used to call NewsAPI.org. */
@Configuration
public class WebClientConfig {

    /** WebClient pre-configured with the NewsAPI base URL from application.properties. */
    @Bean
    WebClient newsApiWebClient(@Value("${newsapi.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
