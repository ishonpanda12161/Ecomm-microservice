package com.ecommerce.app.payload;

import java.time.LocalDateTime;

public record ErrorResponseDTO (
        String type,
        String message,
        Object errors,
        LocalDateTime timestamp
) {}