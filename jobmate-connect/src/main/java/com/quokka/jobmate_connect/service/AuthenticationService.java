package com.quokka.jobmate_connect.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.quokka.jobmate_connect.dto.request.otp.VerifyOtpRequest;
import com.quokka.jobmate_connect.dto.request.user.AuthenticationRequest;
import com.quokka.jobmate_connect.dto.request.user.ExchangeTokenRequest;
import com.quokka.jobmate_connect.dto.request.user.IntrospectRequest;
import com.quokka.jobmate_connect.dto.request.user.LogoutRequest;
import com.quokka.jobmate_connect.dto.response.otp.ResendOtpResponse;
import com.quokka.jobmate_connect.dto.response.user.AuthenticationResponse;
import com.quokka.jobmate_connect.dto.response.user.IntrospectResponse;
import com.quokka.jobmate_connect.dto.request.user.SetPasswordRequest;
import com.quokka.jobmate_connect.dto.response.user.SetPasswordResponse;
import com.quokka.jobmate_connect.dto.response.user.OutboundResponse;
import com.quokka.jobmate_connect.entity.InvalidatedToken;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.repository.InvalidatedTokenRepository;
import com.quokka.jobmate_connect.repository.RoleRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.repository.httpClient.OutboundClient;
import com.quokka.jobmate_connect.repository.httpClient.OutboundUserClient;
import com.quokka.jobmate_connect.kafka.dto.SendOtpEvent;
import com.quokka.jobmate_connect.kafka.topic.OtpEventProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundClient outboundClient;
    OutboundUserClient outboundUserClient;
    OtpService otpService;
    OtpEventProducer otpEventProducer;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    @NonFinal
    @Value("${outbound.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.redirect-url}")
    protected String REDIRECT_URL;

    @NonFinal
    @Value("${outbound.grant-type}")
    protected String GRAND_TYPE;

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException {
        var token = request.getToken();
        boolean isValid = true;
        SignedJWT jwt = null;

        try {
            jwt = verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        String userId = null;
        try {
            if (jwt != null) {
                var claims = jwt.getJWTClaimsSet();
                Object idClaim = claims.getClaim("userId");
                userId = idClaim != null ? String.valueOf(idClaim) : claims.getSubject();
            }
        } catch (Exception ignored) {
        }

        return IntrospectResponse.builder()
                .userId(userId)
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (user.is_two_fa_enabled()) {
            String otp = otpService.generateOtp(user.getId().toString());

            otpEventProducer.sendOtpEvent(SendOtpEvent.builder()
                    .email(user.getEmail())
                    .otp(otp)
                    .timestamp(LocalDateTime.now())
                    .build());

            return AuthenticationResponse.builder()
                    .isTwoFaEnabled(true)
                    .message("OTP has been sent to your email.")
                    .otpExpiryTime(180L)
                    .userId(user.getId().toString())
                    .build();
        }

        var token = generateToken(user);

        log.info("Generated token: {}", token);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public void logout(LogoutRequest request)
            throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already invalidated");
        }
    }

    public AuthenticationResponse refresh(IntrospectRequest request) {
        try {
            var signedJWT = verifyToken(request.getToken(), true);
            var jit = signedJWT.getJWTClaimsSet().getJWTID();
            var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            var email = signedJWT.getJWTClaimsSet().getSubject();

            var user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            invalidatedTokenRepository.save(invalidatedToken);

            var token = generateToken(user);

            return AuthenticationResponse.builder()
                    .token(token)
                    .build();
        } catch (ParseException | JOSEException e) {
            log.error("Cannot refresh token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public AuthenticationResponse outboundAuthenticate(String code) {
        var response = outboundClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URL)
                .grantType(GRAND_TYPE)
                .build());

        OutboundResponse userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("User info: {}", userInfo);

        var existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {

            HashSet<Role> roles = new HashSet<>();
            roleRepository.findByName("USER").ifPresent(roles::add);

            user = User.builder()
                    .email(userInfo.getEmail())
                    .password("")
                    .fullName(userInfo.getName())
                    .contactPhone("")
                    .roles(roles)
                    .build();
            user = userRepository.save(user);
        }

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();

        var token = generateToken(user);

        if (!hasPassword) {
            // User chưa có password, yêu cầu set password
            return AuthenticationResponse.builder()
                    .requiresPasswordSetup(true)
                    .userEmail(user.getEmail())
                    .userName(user.getFullName())
                    .message("Please set up your password to complete registration.")
                    .token(token)
                    .build();
        }

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("quokka.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? Date.from(signedJWT.getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESH_DURATION, ChronoUnit.SECONDS))
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if ((!verified || expiryTime.before(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public AuthenticationResponse verifyOtp(VerifyOtpRequest request) {
        var user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isValid = otpService.validateOtp(request.getUserId(), request.getOtp());

        if (!isValid)
            throw new AppException(ErrorCode.INVALID_OTP);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .isTwoFaEnabled(true)
                .build();
    }

    public ResendOtpResponse resendOtp(String userId) {
        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = otpService.resendOtp(userId);

        otpEventProducer.sendOtpEvent(SendOtpEvent.builder()
                .email(user.getEmail())
                .otp(otp)
                .timestamp(LocalDateTime.now())
                .build());

        return ResendOtpResponse.builder()
                .message("OTP has been resent to your email.")
                .otpExpiryTime(180L)
                .build();
    }

    public SetPasswordResponse setPassword(String userId, SetPasswordRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (request.getPassword().length() < 8) {
            throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (request.getPassword().length() > 50) {
            throw new AppException(ErrorCode.PASSWORD_TOO_LONG);
        }

        var user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_ALREADY_SET);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return SetPasswordResponse.builder()
                .message("Password set successfully!")
                .success(true)
                .redirectUrl("/dashboard")
                .build();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });

        return stringJoiner.toString();
    }
}
