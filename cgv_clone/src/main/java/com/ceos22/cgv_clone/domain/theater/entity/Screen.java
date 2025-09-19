package com.ceos22.cgv_clone.domain.theater.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false)
    private String name; // 1관, IMAX관

    private ScreenType type; // IMAX, DOLBY ATMOS ...

    @OneToMany(mappedBy = "screen")
    private List<Seat> seats = new ArrayList<>();
}
