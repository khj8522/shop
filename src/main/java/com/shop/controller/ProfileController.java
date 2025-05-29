package com.shop.controller;

import com.shop.entity.Member;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    // @AuthenticationPrincipal 어노테이션으로 로그인된 사용자의 정보를 자동으로 주입
    @GetMapping("/members/profile")
    public String viewProfile(@AuthenticationPrincipal Member member, Model model) {
        if (member != null) {
            model.addAttribute("member", member);  // member 객체를 템플릿에 전달
        } else {
            model.addAttribute("member", null);  // 로그인하지 않은 경우
        }
        return "profile";
    }
}
