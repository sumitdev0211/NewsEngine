package com.newsshorts.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NewsApiProperties.class)
public class NewsApiConfig {
}
