package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Getter
@Setter
@ToString
@Table(name = "cart")
public class Cart {

    @Id
    @Column(name="cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;
}
