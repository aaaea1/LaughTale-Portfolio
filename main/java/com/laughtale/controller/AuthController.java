package com.laughtale.controller;

import com.laughtale.domain.Member;
import com.laughtale.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("member", new Member());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute Member member,
                          RedirectAttributes redirectAttributes) {
        try {
        	String encoded = passwordEncoder.encode(member.getPassword());
            memberService.register(member, encoded);
            redirectAttributes.addFlashAttribute("message", "会員登録が完了しました。");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}