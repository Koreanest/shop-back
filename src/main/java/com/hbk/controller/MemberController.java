package com.hbk.controller;


import com.hbk.dto.LoginRequest;
import com.hbk.dto.MemberRegisterRequest;
import com.hbk.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "http://localhost:3000",
        allowCredentials = "true"
)
public class MemberController {

    private final MemberService memberService;

    // 회원가입 + 자동 로그인
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @RequestBody MemberRegisterRequest request,
            HttpSession session
    ) {
        memberService.register(request, session);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpSession session
    ) {
        memberService.login(request, session);

        // 🔍 디버깅용 로그
        System.out.println("LOGIN SESSION ID = " + session.getId());
        System.out.println("LOGIN MEMBER ID = " + session.getAttribute("LOGIN_MEMBER_ID"));

        return ResponseEntity.ok().build();
    }

    // 로그인 상태 확인
    @GetMapping("/members/me")
    public ResponseEntity<?> me(HttpSession session) {
        System.out.println("ME SESSION ID = " + session.getId());
        System.out.println("ME MEMBER ID = " + session.getAttribute("LOGIN_MEMBER_ID"));

        Object memberId = session.getAttribute("LOGIN_MEMBER_ID");
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of("memberId", memberId));
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
