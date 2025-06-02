package com.shop.config;

import com.shop.entity.Member;
import com.shop.entity.PasswordHistory;
import com.shop.repository.MemberRepository;
import com.shop.repository.PasswordHistoryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public CustomLoginSuccessHandler(MemberRepository memberRepository,
                                     PasswordHistoryRepository passwordHistoryRepository) {
        this.memberRepository = memberRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String userEmail = authentication.getName();  // 로그인한 이메일
        Member member = memberRepository.findByEmail(userEmail);

        // 만료 여부 판단
        PasswordHistory latest = passwordHistoryRepository.findTopByMemberIdOrderByChangedAtDesc(member.getId());
        boolean expired = latest == null || latest.getChangedAt().isBefore(LocalDateTime.now().minusMonths(6));
        // 변경 이력이 없거나 6개월 이상 지난경우

        if (expired) {
            // 비밀번호 변경 페이지로 리다이렉트
            response.sendRedirect("/member/changepassword?expired=true");
        } else {
            // 기본 메인 페이지로 이동
            response.sendRedirect("/");
        }
    }
}
