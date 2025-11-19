package com.quokka.jobmate_connect.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GeocodingService {

    private final RestTemplate restTemplate;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?format=json&q={address}";
    private static final double EARTH_RADIUS_KM = 6371.0;

    public double[] getCoordinates(String address) {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(NOMINATIM_URL, JsonNode.class, address);
            JsonNode results = response.getBody();
            if (results != null && results.isArray() && results.size() > 0) {
                double lat = results.get(0).get("lat").asDouble();
                double lon = results.get(0).get("lon").asDouble();
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            log.warn("Cannot fetch coordinates for {} → {}", address, e.getMessage());
        }
        return new double[]{0.0, 0.0};
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double distance = EARTH_RADIUS_KM * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        return Math.round(distance * 100.0) / 100.0;
    }
}
