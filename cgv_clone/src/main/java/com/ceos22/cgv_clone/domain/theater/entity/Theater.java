package com.ceos22.cgv_clone.domain.theater.entity;

import com.ceos22.cgv_clone.domain.region.entity.Region;
import com.ceos22.cgv_clone.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long theaterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false)
    private String name;

    private String address;

    @OneToMany(mappedBy = "theater")
    private List<Screen> screens = new ArrayList<>();

    @OneToMany(mappedBy = "theater") // 나는 Store 엔티티의 'theater' 필드에 의해 매핑된다
    private List<Store> stores = new ArrayList<>();
}
