package com.shop.service;

import com.shop.dto.ReviewDto;
import com.shop.entity.ItemReview;
import com.shop.entity.Member;
import com.shop.entity.OrderItem;
import com.shop.repository.ItemReviewRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ItemReviewRepository itemReviewRepository;
    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;

    public void saveReview(ReviewDto reviewDto, String email) {

        Member member = memberRepository.findByEmail(email);
        if(member == null){
            throw new EntityNotFoundException("회원 정보가 없습니다.");
        }

        OrderItem orderItem = orderItemRepository.findById(reviewDto.getOrderItemId())
                .orElseThrow(() -> new EntityNotFoundException("주문 상품이 없습니다."));

        // 중복 리뷰 작성 방지
        if (itemReviewRepository.findByOrderItem(orderItem) != null) {
            throw new IllegalStateException("이미 이 상품에 대한 리뷰를 작성하셨습니다.");
        }

        ItemReview review = new ItemReview();
        review.setMember(member);
        review.setOrderItem(orderItem);
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        itemReviewRepository.save(review);
    }


    @Transactional(readOnly = true)
    public List<ItemReview> getReviewsByItemId(Long itemId) {
        return itemReviewRepository.findByOrderItem_ItemId(itemId);
    }

    @Transactional(readOnly = true)
    public double getAverageRatingByItemId(Long itemId) {
        Double avg = itemReviewRepository.findAverageRatingByItemId(itemId);
        return avg != null ? avg : 0.0;
    }


}
