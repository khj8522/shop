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
            Member member = Member.createMember(memberFormDto,passwordEncoder,memberFormDto.getRole());
            memberService.saveMember(member);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }
    @GetMapping("/login")
    public String loginMember() {
        return "member/memberLoginForm";  } // 로그인 페이지로 이동


    @GetMapping("/login/error")
    public String loginError(Model model, HttpServletRequest request) {
        String errorMsg = (String) request.getSession().getAttribute("loginErrorMsg");
        Boolean isInactive = (Boolean) request.getSession().getAttribute("accountInactive");
        String email = (String) request.getSession().getAttribute("email");

        if (email == null) {
            email = request.getParameter("email");  // 로그인 실패 시 email 파라미터로 받아옴
            if (email != null && !email.isEmpty()) {
                request.getSession().setAttribute("email", email); // 세션에 이메일 저장
            }
        }

        model.addAttribute("loginErrorMsg", errorMsg);
        model.addAttribute("accountInactive", isInactive != null && isInactive);
        model.addAttribute("email", email); // email을 모델에 전달하여 폼에 표시

        return "member/memberLoginForm";
    }


    @PostMapping("/{memberId}/withdraw")
    public String withdrawMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        memberService.withdrawMember(memberId);

        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute("message","회원 탈퇴가 완료되었습니다.");
        return "redirect:/";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(@RequestParam(required = false) Boolean expired, Model model) {
        model.addAttribute("expired", expired != null && expired);
        return "member/changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal Member member, @RequestParam String currentPassword
                                ,@RequestParam String newPassword, @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {


        if(member == null) {
            return "redirect:/members/login";
        }

        if(!passwordEncoder.matches(currentPassword,member.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/members/change-password";
        }

        if(!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호가 서로 일치하지 않습니다.");
            return "redirect:/members/change-password";
        }

        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findTop3ByMemberOrderByChangedAtDesc(member);

        for(PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("errorMessage", "최근 사용한 비밀번호는 사용할 수 없습니다.");
                return "redirect:/members/change-password";
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

        redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");

        return "redirect:/";
    }

    @PostMapping("/activate")
    public String activateMember(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        // 이메일 값이 제대로 전달되는지 확인
        System.out.println("Email received for activation: " + email);
        if (email == null || email.isEmpty()) {
            System.out.println("Email is missing or empty"); // 디버깅용
            redirectAttributes.addFlashAttribute("errorMessage", "이메일을 찾을 수 없습니다.");
            return "redirect:/members/login";
        }

        try {
            memberService.activateMember(email);
            redirectAttributes.addFlashAttribute("message", "계정이 성공적으로 활성화되었습니다.");
        } catch (Exception e) {
            System.out.println("Error while activating account: " + e.getMessage()); // 예외 출력
            redirectAttributes.addFlashAttribute("errorMessage", "계정 활성화에 실패했습니다.");
        }

        return "redirect:/";
    }

}
