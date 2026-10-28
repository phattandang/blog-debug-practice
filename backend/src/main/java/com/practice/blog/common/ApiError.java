package com.practice.blog.common;

import java.util.Map;

public record ApiError(int status, String error, Map<String, String> details) {

    public static ApiError of(int status, String error) {
        return new ApiError(status, error, Map.of());
    }
}
