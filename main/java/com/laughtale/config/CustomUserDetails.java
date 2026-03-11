package com.laughtale.config;

import com.laughtale.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    
    private final Member member;
    
    public CustomUserDetails(Member member) {
        this.member = member;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + member.getRole())
        );
    }
    
    @Override
    public String getPassword() {
        return member.getPassword();
    }
    
    @Override
    public String getUsername() {
        return member.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return !"SUSPENDED".equals(member.getStatus());
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(member.getStatus());
    }
    
    public Integer getMemberId() {
        return member.getMemberId();
    }
    
    public String getName() {
        return member.getName();
    }
    
    public String getRole() {
        return member.getRole();
    }
}