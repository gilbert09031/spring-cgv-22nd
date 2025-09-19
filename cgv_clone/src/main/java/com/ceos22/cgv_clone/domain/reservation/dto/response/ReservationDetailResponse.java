package com.ceos22.cgv_clone.domain.reservation.dto.response;

import com.ceos22.cgv_clone.domain.reservation.entity.Reservation;
import com.ceos22.cgv_clone.domain.reservation.entity.ReservationStatus;
import com.ceos22.cgv_clone.domain.theater.entity.ScreenType;

import java.time.LocalDateTime;

public record ReservationDetailResponse(
        Long reservationId,
        String movieTitle,
        String posterUrl,
        String theaterName,
        String screenName,
        ScreenType screenType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String rowName,
        String colNumber,
        ReservationStatus status
) {
    // Reservation 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static ReservationDetailResponse from(Reservation reservation) {
        return new ReservationDetailResponse(
                reservation.getReservationId(),
                reservation.getSchedule().getMovie().getTitle(),
                reservation.getSchedule().getMovie().getPosterUrl(),
                reservation.getSchedule().getScreen().getTheater().getName(),
                reservation.getSchedule().getScreen().getName(),
                reservation.getSchedule().getScreen().getType(),
                reservation.getSchedule().getStartTime(),
                reservation.getSchedule().getEndTime(),
                reservation.getSeat().getRowName(),
                reservation.getSeat().getColumnNumber(),
                reservation.getStatus()
        );
    }
}
