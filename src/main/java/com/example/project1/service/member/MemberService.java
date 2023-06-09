package com.example.project1.service.member;

import com.example.project1.config.jwt.JwtAuthenticationFilter;
import com.example.project1.config.jwt.JwtProvider;
import com.example.project1.domain.jwt.TokenDTO;
import com.example.project1.domain.member.MemberDTO;
import com.example.project1.entity.jwt.TokenEntity;
import com.example.project1.entity.member.MemberEntity;
import com.example.project1.repository.jwt.TokenRepository;
import com.example.project1.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    // 회원가입
    public String signUp(MemberDTO memberDTO) throws Exception {

        try {
            MemberEntity byUserEmail = memberRepository.findByUserEmail(memberDTO.getUserEmail());

            if (byUserEmail != null) {
                return "이미 가입된 회원입니다.";
            }

            // 아이디가 없다면 DB에 넣어서 등록 해준다.
            MemberEntity member = MemberEntity.builder()
                    .userEmail(memberDTO.getUserEmail())
                    .userPw(passwordEncoder.encode(memberDTO.getUserPw()))
                    .userName(memberDTO.getUserName())
                    .nickName(memberDTO.getNickName())
                    .userType(memberDTO.getUserType())
                    .provider(memberDTO.getProvider())
                    .providerId(memberDTO.getProviderId())
                    .build();

            log.info("member : " + member);
            MemberEntity save = memberRepository.save(member);

//            MemberDTO memberDTO1 = MemberDTO.toMemberDTO(Optional.of(save));

            return "회원가입에 성공했습니다.";
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e; // 예외를 던져서 예외 처리를 컨트롤러로 전달
        }

    }

    // 아이디 조회
    public MemberDTO search(Long userId) {
        Optional<MemberEntity> searchId = memberRepository.findById(userId);
        MemberDTO memberDTO = MemberDTO.toMemberDTO(searchId);
        return memberDTO;
    }

    // 로그인
    public ResponseEntity<TokenDTO> login(String userEmail, String userPw) throws Exception {

//        // Login ID/PW를 기반으로 UsernamePasswordAuthenticationToken 생성
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(userEmail, userPw);
//
//        log.info("----------------------");
//        log.info("authenticationToken : " +authenticationToken);
//        log.info("----------------------");
//
//        // 실제 검증(사용자 비밀번호 체크)이 이루어지는 부분
//        // authenticateToken을 이용해서 Authentication 객체를 생성하고
//        // authentication 메서드가 실행될 때
//        // CustomUserDetailsService에서 만든 loadUserbyUsername 메서드가 실행
//        Authentication authentication = authenticationManagerBuilder
//                .getObject().authenticate(authenticationToken);
//
//        log.info("----------------------");
//        log.info("authentication : " + authentication);
//        log.info("----------------------");
//
//        // 해당 객체를 SecurityContextHolder에 저장
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // authentication 객체를 createToken 메소드를 통해서 생성
//        // 인증 정보를 기반으로 생성
//        TokenDTO tokenDTO = jwtProvider.createToken(authentication);
//
//        log.info("----------------------");
//        log.info("tokenDTO : " + tokenDTO);
//        log.info("----------------------");
//
//        HttpHeaders headers = new HttpHeaders();
//
//        // response header에 jwt token을 넣어줌
//        headers.add(JwtAuthenticationFilter.HEADER_AUTHORIZATION, "Bearer " + tokenDTO);
//
//        log.info("----------------------");
//        log.info("headers : " + headers);
//        log.info("----------------------");
//
//        MemberEntity member = memberRepository.findByUserEmail(userEmail);
//        log.info("member : " + member);
//
//        TokenEntity tokenEntity = TokenEntity.builder()
//                .grantType(tokenDTO.getGrantType())
//                .accessToken(tokenDTO.getAccessToken())
//                .refreshToken(tokenDTO.getRefreshToken())
//                .userEmail(tokenDTO.getUserEmail())
//                .nickName(member.getNickName())
//                .userId(member.getUserId())
//                .build();
//
//        log.info("token : " + tokenEntity);
//
//        tokenRepository.save(tokenEntity);
//
//        return new ResponseEntity<>(tokenDTO, headers, HttpStatus.OK);

        MemberEntity findUser = memberRepository.findByUserEmail(userEmail);
        log.info("findUser : " + findUser);

        if (findUser != null) {

            Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, userPw);

            TokenDTO token = jwtProvider.createToken(authentication);

            //        // Login ID/PW를 기반으로 UsernamePasswordAuthenticationToken 생성


            token = TokenDTO.builder()
                    .grantType(token.getGrantType())
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .userEmail(findUser.getUserEmail())
                    .nickName(findUser.getNickName())
                    .userId(findUser.getUserId())
                    .build();


            TokenEntity tokenEntity = TokenEntity.builder()
                    .id(token.getId())
                    .grantType(token.getGrantType())
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .userEmail(token.getUserEmail())
                    .nickName(token.getNickName())
                    .userId(token.getUserId())
                    .build();

            log.info("token : " + tokenEntity);
            tokenRepository.save(tokenEntity);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return null;
        }
    }


    // 회원정보 수정
    public MemberDTO update(MemberDTO memberDTO) {

        MemberEntity member = MemberEntity.builder()
                .userEmail(memberDTO.getUserEmail())
                .userPw(passwordEncoder.encode(memberDTO.getUserPw()))
                .userName(memberDTO.getUserName())
                .nickName(memberDTO.getNickName())
                .userType(memberDTO.getUserType())
                .provider(memberDTO.getProvider())
                .providerId(memberDTO.getProviderId())
                .build();

        memberRepository.save(member);

        // 제대로 DTO 값이 엔티티에 넣어졌는지 확인하기 위해서
        // 엔티티에 넣어주고 다시 DTO 객체로 바꿔서 리턴을 해줬습니다.
        MemberDTO memberDto = MemberDTO.toMemberDTO(Optional.of(member));
        log.info("memberDto : " + memberDto);
        return memberDto;
    }
}
