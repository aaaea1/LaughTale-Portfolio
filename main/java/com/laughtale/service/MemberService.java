package com.laughtale.service;

import com.laughtale.domain.Member;
import com.laughtale.config.CustomUserDetails;
import com.laughtale.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return new CustomUserDetails(member);
    }
    
    // 회원 가입
    public Member register(Member member, String encodedPassword) {
        if (memberRepository.existsByUsername(member.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        member.setPassword(encodedPassword);
        
        if (member.getRole() == null) {
            member.setRole("USER");
        }
        if (member.getStatus() == null) {
            member.setStatus("ACTIVE");
        }
        
        return memberRepository.save(member);
    }
    
    // 회원 정보 수정
    public Member updateMember(Integer memberId, Member updatedMember) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        member.setName(updatedMember.getName());
        member.setEmail(updatedMember.getEmail());
        member.setPhone(updatedMember.getPhone());
        
        return memberRepository.save(member);
    }
    
    // 비밀번호 변경
    public void changePassword(Integer memberId, String oldPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
    
    @Transactional(readOnly = true)
    public Page<Member> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Member> searchMembers(String keyword, Pageable pageable) {
        return memberRepository.findByNameContaining(keyword, pageable);
    }
    
    @Transactional(readOnly = true)
    public Member getMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    
    public void deleteMember(Integer memberId) {
        memberRepository.deleteById(memberId);
    }
    
    @Transactional(readOnly = true)
    public long getActiveMemberCount() {
        return memberRepository.countActiveMembers();
    }

    public void updateMemberStatus(Integer memberId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.setStatus(status);
        memberRepository.save(member);
    }
}