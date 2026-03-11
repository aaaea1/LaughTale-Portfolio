package com.laughtale.repository;

import com.laughtale.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    
    // 사용자명으로 조회
    Optional<Member> findByUsername(String username);
    
    // 이메일로 조회
    Optional<Member> findByEmail(String email);
    
    // 사용자명 중복 체크
    boolean existsByUsername(String username);
    
    // 이메일 중복 체크
    boolean existsByEmail(String email);
    
    // 상태별 회원 조회
    Page<Member> findByStatus(String status, Pageable pageable);
    
    // 역할별 회원 조회
    Page<Member> findByRole(String role, Pageable pageable);
    
    // 이름으로 검색
    Page<Member> findByNameContaining(String name, Pageable pageable);
    
    // 활성 회원 수 조회
    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = 'ACTIVE'")
    long countActiveMembers();
}