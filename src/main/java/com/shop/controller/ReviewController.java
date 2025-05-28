package com.shop.controller;

import com.shop.dto.ReviewDto;
import com.shop.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<String> saveReview(@RequestBody ReviewDto reviewDto, Principal principal) {
        if(principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            reviewService.saveReview(reviewDto, principal.getName());
            return new ResponseEntity<>("리뷰 작성 완료", HttpStatus.OK);
        } catch(EntityNotFoundException e) {
            return new ResponseEntity<>("회원 또는 주문상품 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



}