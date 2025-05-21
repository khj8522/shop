package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 하나의 상품은 여러 주문의 상품으로 들어감 -> 여러 주문의 상품 기준으로 다대일

    @ManyToOne(fetch = FetchType.LAZY) //지연 로딩
    @JoinColumn(name = "order_id") // 주문과 매핑 (order_id를 외래키로 가짐)(외래키의 주인)
    private Order order;

    private int orderPrice;

    private int count;



}