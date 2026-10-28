package com.practice.blog.post;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PostRequest(
        @NotBlank(message = "title is required") String title,
        @NotBlank(message = "content is required") String content,
        @NotBlank(message = "category is required") String category,
        String excerpt,
        List<String> tags,
        @PositiveOrZero(message = "readTime must be 0 or more") Integer readTime
) {
}
