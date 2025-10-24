package com.ceos22.cgv_clone.domain.order.entity;

import com.ceos22.cgv_clone.common.entity.BaseEntity;
import com.ceos22.cgv_clone.domain.member.entity.Member;
import com.ceos22.cgv_clone.domain.store.entity.Product;
import com.ceos22.cgv_clone.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // DB 키워드인 ORDER와 겹치지 않게 테이블 이름 명시
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    private Order(Member member, Store store) {
        this.member = member;
        this.store = store;
        this.status = OrderStatus.CART;
        this.totalPrice = 0;
    }

    public static Order of(Member member, Store store) {
        return new Order(member, store);
    }
    //==================================================================================================================
    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);
        orderDetail.setOrder(this);
        calculateTotalPrice();
    }

    public void removeOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.remove(orderDetail);
        orderDetail.setOrder(null);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        this.totalPrice = orderDetails.stream()
                .mapToInt(OrderDetail::getSubtotal)
                .sum();
    }
    //=====================장바구니======================================================================================

    public void addToCart(Product product, Integer quantity) {
        validateCartStatus();

        OrderDetail existingDetail = findOrderDetailsByProduct(product);

        if (existingDetail != null) {
            existingDetail.changeQuantity(existingDetail.getQuantity() + quantity);
            calculateTotalPrice();
        } else {
            OrderDetail newOrderDetail = OrderDetail.of(product, quantity);
            addOrderDetail(newOrderDetail);
        }
    }

    public void removeFromCart(Long productId) {
        validateCartStatus();

        OrderDetail orderDetail = findOrderDetailsByProductId(productId);
        if (orderDetail == null) {
            throw new IllegalArgumentException("장바구니에 해당 상품 없음");
        }
        removeOrderDetail(orderDetail);
    }

    public void updateCartItemQuantity(Long productId, Integer quantity) {
        validateCartStatus();

        OrderDetail orderDetail = findOrderDetailsByProductId(productId);
        if (orderDetail == null) {
            throw new IllegalArgumentException("장바구니에 해당 상품 없음");
        }

        orderDetail.changeQuantity(quantity);
        calculateTotalPrice();
    }

    public void clearCart() {
        validateCartStatus();
        orderDetails.clear();
        calculateTotalPrice();
    }
    // ================================================================================================================
    public void confirmOrder() {
        validateCartStatus();
        validateCartNotEmpty();
        this.status = OrderStatus.CONFIRMED;
    }

    public void completeOrder() {
        validateConfirmedStatus();
        this.status = OrderStatus.COMPLETED;
    }

    // ================================================================================================================

    private OrderDetail findOrderDetailsByProduct(Product product) {
        return orderDetails.stream()
                .filter(detail -> detail.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }

    private OrderDetail findOrderDetailsByProductId(Long productId) {
        return orderDetails.stream()
                .filter(detail -> detail.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void validateCartStatus() {
        if(this.status != OrderStatus.CART) {
            throw new IllegalStateException("장바구니 상태가 아닙니다");
        }
    }

    private void validateConfirmedStatus() {
        if(this.status != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("주문완료 상태가 아닙니다");
        }
    }

    private void validateCartNotEmpty() {
        if(orderDetails.isEmpty()){
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }
    }
}
