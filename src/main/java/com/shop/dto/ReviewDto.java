package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private Long orderItemId; // 어떤 주문 상품에 대한 리뷰인지
    private int rating;       // 별점 (1~5)
    private String content;   // 리뷰 내용
}
