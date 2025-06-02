package com.shop.controller;

import com.shop.entity.Member;
import com.shop.entity.PasswordHistory;
import com.shop.repository.MemberRepository;
import com.shop.repository.PasswordHistoryRepository;
import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            Model model) {

        Member member = memberRepository.findByEmail(principal.getName());
        if (member == null) {
            model.addAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            model.addAttribute("member", null);
            return "profile";  // 리다이렉트 하지 않고 뷰를 바로 보여줌
        }

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            model.addAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            model.addAttribute("member", member);
            return "profile";  // 에러 메시지와 함께 뷰를 렌더링
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "새 비밀번호가 서로 일치하지 않습니다.");
            model.addAttribute("member", member);
            return "profile";
        }

        // 🔒 최근 비밀번호 이력 3개 확인
        List<PasswordHistory> recentPasswords =
                passwordHistoryRepository.findTop3ByMemberOrderByChangedAtDesc(member);

        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                model.addAttribute("errorMessage", "최근 사용한 비밀번호는 사용할 수 없습니다.");
                model.addAttribute("member", member);
                return "profile";
            }
        }

        // 새 비밀번호로 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encodedPassword);
        memberRepository.save(member);

        // 비밀번호 이력 저장
        PasswordHistory history = PasswordHistory.builder()
                .member(member)
                .passwordHash(encodedPassword)
                .changedAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(history);

        return "redirect:/members/profile?passwordChanged=true";  // 성공 시에만 리다이렉트
    }

}
