package com.ceos22.cgv_clone.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemUpdateRequest(
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
