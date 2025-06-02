package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.entity.PasswordHistory;
import com.shop.repository.MemberRepository;
import com.shop.repository.PasswordHistoryRepository;
import com.shop.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    @GetMapping(value = "/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto",new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto,passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }
    @GetMapping("/login")
    public String loginMember() {
        return "member/memberLoginForm";  // 로그인 페이지로 이동
    }

    @GetMapping("/login/error")
    public String loginError(Model model, HttpServletRequest request) {
        String errorMsg = (String) request.getSession().getAttribute("loginErrorMsg");
        Boolean isInactive = (Boolean) request.getSession().getAttribute("accountInactive");

        model.addAttribute("loginErrorMsg",errorMsg);
        model.addAttribute("accountInactive",isInactive != null && isInactive);
        return "member/memberLoginForm";
    }

    @PostMapping("/{memberId}/withdraw")
    public String withdrawMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        memberService.withdrawMember(memberId);

        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute("message","회원 탈퇴가 완료되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/members/change-password")
    public String changePasswordForm(@RequestParam(required = false) Boolean expired, Model model) {
        model.addAttribute("expired", expired != null && expired);
        return "member/changePassword";
    }

    @PostMapping("/members/change-password")
    public String changePassword(@AuthenticationPrincipal Member member, @RequestParam String currentPassword
                                ,@RequestParam String newPassword, @RequestParam String confirmPassword, Model model) {



        if(member == null) {
            return "redirect:/members/login";
        }

        if(!passwordEncoder.matches(currentPassword,member.getPassword())) {
            model.addAttribute("errormessage", "현재 비밀번호가 일치하지 않습니다.");
            return "member/changePassword";
        }

        if(!passwordEncoder.matches(newPassword,confirmPassword)) {
            model.addAttribute("errormessage", "새 비밀번호가 서로 일치하지 않습니다.");
            return "member/changePassowrd";
        }

        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findTop3ByMemberOrderByChangedAtDesc(member);

        for(PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                model.addAttribute("errormessage", "최근 사용한 비밀번호는 사용할 수 없습니다.");
                return "member/changePassword";
            }
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encodedNewPassword);
        memberRepository.save(member);

        PasswordHistory newHistory = PasswordHistory.builder()
                .member(member)
                .passwordHash(encodedNewPassword)
                .changedAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(newHistory);

        return "redirect:/";
    }
}
