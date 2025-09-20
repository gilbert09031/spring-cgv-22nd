package com.ceos22.cgv_clone.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReservationCreateRequest(
        @NotBlank(message = "회원 ID는 필수입니다")
        String memberId,

        @NotBlank(message = "스케쥴 ID는 필수입니다")
        Long scheduleId,

        @NotBlank(message = "좌석 ID는 필수입니다")
        Long seatId
) {
}
