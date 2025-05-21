package com.shop.entity;

import com.shop.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue
    @Column(name="order_id")
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id") // member와 매핑
    private Member member;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    //join은 외래키를 기준으로 생각(join 하는쪽이 주인)(mappedBy에 주인의 필드를 적어줌)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>(); //주문을 넣으면 주문상품을 저장

    private LocalDateTime regTime;

    private LocalDateTime updateTime;

}
