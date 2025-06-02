package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import com.shop.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="member")
@Getter
@Setter
@ToString
public class Member extends  BaseEntity implements UserDetails {

    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_active",nullable = false)
    private boolean active = true;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역할을 기반으로 권한을 반환
        return List.of(() -> "ROLE_" + role.name());
    }

    @Override
    public String getUsername() {
        return email;  // 이메일을 사용자 이름으로 사용
    }

    @Override
    public String getPassword() {
        return password;  // 비밀번호 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 자격 증명 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return active; // false이면 로그인 차단 (게정 활성화 여부)
    }

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        member.setActive(true);
        return member;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordHistory> passwordHistories = new ArrayList<>();

    public void addPasswordHistory(PasswordHistory passwordHistory) {
        passwordHistories.add(passwordHistory);
        passwordHistory.setMember(this);
    }



}