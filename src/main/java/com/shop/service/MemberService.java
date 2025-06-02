package com.shop.service;

import com.shop.entity.Member;
import com.shop.entity.PasswordHistory;
import com.shop.repository.MemberRepository;
import com.shop.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    private final PasswordHistoryRepository passwordHistoryRepository;

    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        Member savedMember = memberRepository.save(member);

        // 비밀번호 이력 저장 (회원가입 시)
        PasswordHistory history = new PasswordHistory();
        history.setMember(savedMember);
        history.setPasswordHash(savedMember.getPassword());
        history.setChangedAt(LocalDateTime.now().minusMonths(10));

        passwordHistoryRepository.save(history);

        return savedMember;
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new UsernameNotFoundException(email);
        }

        if (!member.isActive()) {
            throw new DisabledException("탈퇴한 계정입니다.");
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 회원이 없습니다."));

        member.setActive(false); // soft delete 처리
    }

    public boolean isPasswordExpired(Member member) {
        PasswordHistory latest = passwordHistoryRepository.findTopByMemberOrderByChangedAtDesc(member);
        // 최신 갱신기간
        if(latest == null) return true; // 변경 이력이 없다면 true 반환
        return latest.getChangedAt().isBefore(LocalDateTime.now().minusMonths(6)); // 6개월 이전이면 true 빈환
    }
}
