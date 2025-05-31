package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

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

}
