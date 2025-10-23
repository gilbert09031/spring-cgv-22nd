package com.ceos22.cgv_clone.domain.auth.service;

import com.ceos22.cgv_clone.common.error.CustomException;
import com.ceos22.cgv_clone.common.error.ErrorCode;
import com.ceos22.cgv_clone.domain.auth.jwt.JwtTokenProvider;
import com.ceos22.cgv_clone.domain.auth.jwt.error.JwtException;
import com.ceos22.cgv_clone.domain.member.dto.request.LoginRequest;
import com.ceos22.cgv_clone.domain.member.dto.request.RefreshTokenRequest;
import com.ceos22.cgv_clone.domain.member.dto.request.SignUpRequest;
import com.ceos22.cgv_clone.domain.member.dto.response.MemberInfoResponse;
import com.ceos22.cgv_clone.domain.member.dto.response.TokenResponse;
import com.ceos22.cgv_clone.domain.member.entity.Member;
import com.ceos22.cgv_clone.domain.member.entity.Role;
import com.ceos22.cgv_clone.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";

    public MemberInfoResponse signUp(SignUpRequest request) {

        log.info("회원가입 처리 시작 {}", request.email());

        if(memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }   

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .birthDate(request.birthDate())
                .role(Role.USER)
                .build();

        Member savedMember = memberRepository.save(member);

        log.info("회원가입 성공 {}", request.email());

        return MemberInfoResponse.from(savedMember);
    }

    public TokenResponse login(LoginRequest request) {
        log.info("로그인 처리 시작 {}", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);

        String redisKey = REFRESH_TOKEN_PREFIX + member.getMemberId();
        redisService.setValueWithExpiration(
                redisKey,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        log.info("로그인 처리 완료 {}", request.email());

        return TokenResponse.of(
                accessToken,
                refreshToken
        );
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        jwtTokenProvider.validateToken(refreshToken);

        String userId = jwtTokenProvider.getTokenUserId(refreshToken);

        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisService.getValue(redisKey);

        // 이미 삭제된 RT 또는 재발급 되어 새로운 토큰이 저장되어 있을 경우 모든 RT 초기화
        if(storedToken == null || !storedToken.equals(refreshToken)) {
            redisService.deleteValue(redisKey);
            throw new JwtException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findById(Long.parseLong(userId))
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        redisService.deleteValue(redisKey);

        String newAccessToken = jwtTokenProvider.createAccessToken(member);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member);

        redisService.setValueWithExpiration(
                redisKey,
                newRefreshToken,
                7,
                TimeUnit.DAYS
        );

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken
        );
    }

    public void logout(String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }

        String userId = jwtTokenProvider.getTokenUserId(accessToken);

        // AT의 유효기간동안 블랙리스트로 저장
        if (accessToken != null && accessToken.isEmpty()) {
            try {
                long expiration = jwtTokenProvider.getExpiration(accessToken);
                String blackListKey = BLACKLIST_PREFIX + accessToken;
                redisService.setValueWithExpiration(
                        blackListKey,
                        "logout",
                        expiration,
                        TimeUnit.MILLISECONDS
                );
            } catch(Exception e) {
                log.error("AccessToken 블랙리스트 등록 실패 : {}", e.getMessage());
            }
        }

        // Redis에 저장된 RT 삭제
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        redisService.deleteValue(refreshTokenKey);

        SecurityContextHolder.clearContext();
    }



}
