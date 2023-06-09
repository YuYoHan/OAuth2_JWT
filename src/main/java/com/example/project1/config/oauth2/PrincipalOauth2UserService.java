package com.example.project1.config.oauth2;

import com.example.project1.config.auth.PrincipalDetails;
import com.example.project1.config.oauth2.provider.GoogleUserInfo;
import com.example.project1.config.oauth2.provider.NaverUserInfo;
import com.example.project1.config.oauth2.provider.OAuth2UserInfo;
import com.example.project1.domain.member.MemberDTO;
import com.example.project1.domain.member.UserType;
import com.example.project1.entity.member.MemberEntity;
import com.example.project1.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private  BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRepository memberRepository;

    // 구글로부터 받은 userReuest 데이터에 대한 후처리되는 함수

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // registrationId로 어떤 OAuth로 로그인 했는지 확인가능
        log.info("clientRegistration : " + userRequest.getClientRegistration() );
        log.info("accessToken : " + userRequest.getAccessToken().getTokenValue() );

        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 구글 로그인 버튼 클릭 →구글 로그인 창 → 로그인 완료 → code 를 리턴(OAuth-Client 라이브러리) → AccessToken 요청
        // userRequest 정보 → 회원 프로필 받아야함(loadUser 함수 호출) → 구글로부터 회원 프로필을 받아준다.
        log.info("getAttributes : " + oAuth2User.getAttributes());

        // 회원가입을 강제로 진행
        OAuth2UserInfo oAuth2UserInfo = null;

        if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            log.info("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            log.info("네이버 로그인 요청");
            // 네이버는 response를 json으로 리턴을 해주는데 아래의 코드가 받아오는 코드다.
            // response={id=5SN-ML41CuX_iAUFH6-KWbuei8kRV9aTHdXOOXgL2K0, email=zxzz8014@naver.com, name=전혜영}
            // 위의 정보를 NaverUserInfo에 넘기면
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        } else {
            log.info("구글과 네이버만 지원합니다.");
        }

        String provider = Objects.requireNonNull(oAuth2UserInfo).getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        // 예) google_109742856182916427686
        String userName = provider + "_" + providerId;
        String password = bCryptPasswordEncoder.encode("get");
        String email = oAuth2UserInfo.getEmail();
        UserType role = UserType.USER;


        MemberEntity member = memberRepository.findByUserEmail(email);

        if(member == null) {
            log.info("OAuth 로그인이 최초입니다.");
            member = MemberEntity.builder()
                    .userName(userName)
                    .userPw(password)
                    .userEmail(email)
                    .userType(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            memberRepository.save(member);


        } else {
            log.info("로그인을 이미 한적이 있습니다. 당신은 자동회원가입이 되어 있습니다.");
        }
        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }
}
