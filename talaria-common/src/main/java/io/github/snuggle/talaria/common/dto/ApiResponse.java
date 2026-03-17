package io.github.snuggle.talaria.common.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        String errorCode
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, String errorCode) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
}
