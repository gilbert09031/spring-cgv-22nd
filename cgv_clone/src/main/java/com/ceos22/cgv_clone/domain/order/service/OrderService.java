package com.ceos22.cgv_clone.domain.order.service;

import com.ceos22.cgv_clone.common.error.CustomException;
import com.ceos22.cgv_clone.common.error.ErrorCode;
import com.ceos22.cgv_clone.domain.member.entity.Member;
import com.ceos22.cgv_clone.domain.member.repository.MemberRepository;
import com.ceos22.cgv_clone.domain.order.dto.request.OrderCreateRequest;
import com.ceos22.cgv_clone.domain.order.dto.response.OrderResponse;
import com.ceos22.cgv_clone.domain.order.entity.Order;
import com.ceos22.cgv_clone.domain.order.entity.OrderDetail;
import com.ceos22.cgv_clone.domain.order.entity.OrderStatus;
import com.ceos22.cgv_clone.domain.order.repository.OrderRepository;
import com.ceos22.cgv_clone.domain.store.entity.Store;
import com.ceos22.cgv_clone.domain.store.repository.StoreRepository;
import com.ceos22.cgv_clone.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final StoreService storeService;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {

        Member member = findMemberById(memberId);
        Store store = findStoreById(request.storeId());

        Order cart = getCart(member, store);

        validateCartNotEmpty(cart);
        decreaseStockForOrder(cart);

        cart.confirmOrder();

        return OrderResponse.from(cart);
    }

    public List<OrderResponse> getMyOrders(Long memberId) {

        Member member = findMemberById(memberId);

        List<Order> allOrders = orderRepository.findByMember(member);

        List<Order> orders = allOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CART)
                .collect(Collectors.toList());

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderDetail(Long memberId, Long orderId) {

        Member member = findMemberById(memberId);
        Order order = findOrderById(orderId);

        validateOrderOwner(order, member);
        validateOrderNotCart(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {

        Order order = findOrderById(orderId);
        order.completeOrder();
    }

    //=================================================================================================================
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Order getCart(Member member, Store store) {
        List<Order> carts = orderRepository.findByMemberAndOrderStatus(member, OrderStatus.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        return carts.stream()
                .filter(order -> order.getStore().getStoreId().equals(store.getStoreId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }
    //=================================================================================================================
    private void validateCartNotEmpty(Order cart) {
        if (cart.getOrderDetails().isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }
    }

    private void validateOrderOwner(Order order, Member member) {
        if (!order.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.AUTHORIZATION_FAILED);
        }
    }

    private void validateOrderNotCompleted(Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_COMPLETED);
        }
    }

    private void validateOrderNotCart(Order order) {
        if (order.getStatus() == OrderStatus.CART) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }
    }

    //=================================================================================================================
    private void decreaseStockForOrder(Order cart) {
        for (OrderDetail detail : cart.getOrderDetails()) {
            try {
                storeService.decreaseStock(
                        cart.getStore().getStoreId(),
                        detail.getProduct().getProductId(),
                        detail.getQuantity()
                );
            } catch (Exception e) {
                throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
    }
}
