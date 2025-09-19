package com.ceos22.cgv_clone.domain.store.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class StoreStockId implements Serializable {
    private Long store; // Store 엔티티의 store 필드와 이름 일치
    private Long product; // Store 엔티티의 product 필드와 이름 일치
}