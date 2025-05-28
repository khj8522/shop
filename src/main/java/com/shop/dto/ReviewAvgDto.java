package com.shop.dto;

import com.shop.entity.ItemReview;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewAvgDto {
    private String memberEmail;
    private String content;
    private int rating;
    private LocalDateTime createdAt;

    public ReviewAvgDto(ItemReview review) {
        this.memberEmail = review.getMember().getEmail();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.createdAt = review.getCreatedAt();
    }
}
