package com.shop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component     // 자동으로 관리
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    // spring security에서 로그인 실패시 동작을 커스터마이징하기 위한 핸들러

    // 리다이렉트 처리를 담당
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override   // 실패시 자동을 호출되는 콜백 메서드
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
    // 로그인 요청 객체, 응답 객체, 실패 이유가 담긴 예외 객체를 받음

        // 기본 에러메세지
        String errorMsg = "아이디 또는 비밀번호를 확인해주세요";


        // 만약 비활성화된 계정 예외가 발생하면
        if (exception instanceof DisabledException) {
            errorMsg = "비활성화된 계정입니다. 아래 버튼을 통해 계정을 활성화해주세요.";
            request.getSession().setAttribute("accountInactive", true);
        }

        request.getSession().setAttribute("loginErrorMsg", errorMsg);

        // 로그인 실패 시 /login/error로 redirect
        redirectStrategy.sendRedirect(request, response, "/members/login/error");
    }
}
