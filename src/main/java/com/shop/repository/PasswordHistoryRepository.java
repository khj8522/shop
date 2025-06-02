package com.shop.repository;

import com.shop.entity.Member;
import com.shop.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop3ByMemberOrderByChangedAtDesc(Member member);
    PasswordHistory findTopByMemberOrderByChangedAtDesc(Member member);
    PasswordHistory findTopByMemberIdOrderByChangedAtDesc(Long memberId);
}
