package com.shop.controller;

import com.shop.dto.ReviewAvgDto;
import com.shop.entity.ItemReview;
import com.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping("/api/reviews/item/{itemId}")
    public ResponseEntity<?> getReviewsAndAverage(@PathVariable Long itemId) {
        try {
            List<ItemReview> reviews = reviewService.getReviewsByItemId(itemId);
            List<ReviewAvgDto> reviewDtos = reviews.stream()
                    .map(ReviewAvgDto::new)
                    .collect(Collectors.toList());

            double averageRating = reviewService.getAverageRatingByItemId(itemId);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviewDtos);
            response.put("averageRating", averageRating);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("리뷰 조회 중 오류가 발생했습니다.");
        }
    }
}
