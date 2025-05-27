package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "item_review")
public class ItemReview {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 리뷰 작성자(회원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    private int rating;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private LocalDateTime createdAt = LocalDateTime.now();
}
