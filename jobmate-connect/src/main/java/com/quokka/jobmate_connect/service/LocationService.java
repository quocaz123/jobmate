package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.dto.request.user.LocationRequest;
import com.quokka.jobmate_connect.dto.response.user.LocationResponse;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {
    UserRepository userRepository;
    GeocodingService geocodingService;
    RestTemplate restTemplate;

    public void updateLocation(LocationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getLatitude() != null && user.getLongitude() != null) {
            throw new AppException(ErrorCode.LOCATION_ALREADY_SET);
        }


        if (request.getAddress() != null && (request.getLatitude() == null || request.getLongitude() == null)) {
            double[] coordinates = geocodingService.getCoordinates(request.getAddress());
            if (coordinates == null) {
                throw new AppException(ErrorCode.GEOCODING_FAILED);
            }
            user.setLatitude(coordinates[0]);
            user.setLongitude(coordinates[1]);
            user.setAddress(request.getAddress());
        } else {
            if (request.getLatitude() != null && request.getLongitude() != null) {
                // Validate coordinates
                if (request.getLatitude() < -90 || request.getLatitude() > 90 ||
                    request.getLongitude() < -180 || request.getLongitude() > 180) {
                    throw new AppException(ErrorCode.INVALID_COORDINATES);
                }
            }
            user.setLatitude(request.getLatitude());
            user.setLongitude(request.getLongitude());
        }

        userRepository.save(user);
    }

    // Lấy vị trí tự động từ IP
    public LocationResponse getAutoLocation(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        try {
            var result = restTemplate.getForObject("http://ip-api.com/json/" + ip +
                            "?fields=lat,lon,city,query",
                    LocationResponse.class);

            return new LocationResponse(
                    null,
                    result.getCity(),
                    result.getLatitude(),
                    result.getLongitude()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

    }
}
