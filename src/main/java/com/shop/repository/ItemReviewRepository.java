package com.shop.repository;

import com.shop.entity.ItemReview;
import com.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {
    ItemReview findByOrderItem(OrderItem orderItem);

    // 주문 상품(OrderItem)과 연결된 Item의 id로 리뷰 목록 조회
    List<ItemReview> findByOrderItem_ItemId(Long itemId);

    // 특정 상품의 리뷰 평점 평균 조회
    @Query("SELECT AVG(r.rating) FROM ItemReview r WHERE r.orderItem.item.id = :itemId")
    Double findAverageRatingByItemId(@Param("itemId") Long itemId);

}
