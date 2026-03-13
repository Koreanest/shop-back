package com.hbk.service;

import com.hbk.dto.LoginRequest;
import com.hbk.dto.MemberRegisterRequest;
import com.hbk.entity.Member;
import com.hbk.global.exception.BadRequestException;
import com.hbk.global.exception.ConflictException;
import com.hbk.global.exception.ErrorCode;
import com.hbk.global.exception.UnauthorizedException;
import com.hbk.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";
    private static final int SESSION_EXPIRE_SECONDS = 60 * 60 * 48; // 48시간

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 + 자동 로그인
    public Member register(MemberRegisterRequest request, HttpSession session) {

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            // 기존: throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            // 변경: 회원가입 입력값 검증 실패이므로 400
            throw new BadRequestException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (memberRepository.existsByEmail(request.getEmail())) {
            // 기존: throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            // 변경: 중복 리소스 충돌이므로 409
            throw new ConflictException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .tel(request.getTel())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .build();

        Member saved = memberRepository.save(member);

        // 회원가입 즉시 로그인
        session.setAttribute(LOGIN_MEMBER_ID, saved.getId());
        session.setMaxInactiveInterval(SESSION_EXPIRE_SECONDS);

        return saved;
    }

    // 로그인
    public void login(LoginRequest request, HttpSession session) {
        Member member = memberRepository.findByEmail(request.getEmail())
                // 기존: orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));
                // 변경: 로그인 실패는 이메일 존재 여부를 노출하지 않고 401로 통일
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            // 기존: throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            // 변경: 로그인 인증 실패이므로 401
            throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
        }

        session.setAttribute(LOGIN_MEMBER_ID, member.getId());
        session.setMaxInactiveInterval(SESSION_EXPIRE_SECONDS);
    }

    // 로그아웃
    public void logout(HttpSession session) {
        session.invalidate();
    }
}