package com.shop.repository;

import com.shop.entity.ItemReview;
import com.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {
    ItemReview findByOrderItem(OrderItem orderItem);
}
