package com.creditcardadvisor.service;

import com.creditcardadvisor.dto.StoreInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GooglePlacesService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Detect nearest store and category using latitude & longitude.
     */

    public List<StoreInfo> detectNearbyStores(double latitude, double longitude) {
        String placesUrl = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=100&type=store&key=%s",
                latitude, longitude, googleApiKey
        );

        Map<String, Object> response = restTemplate.getForObject(placesUrl, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        if (results == null || results.isEmpty()) return List.of();

        return results.stream()
                .limit(5) // top 5
                .map(r -> {
                    String storeName = (String) r.get("name");
                    String category = getCategoryForStore(storeName); // reuse your logic
                    return new StoreInfo(storeName, category);
                })
                .toList();
    }

    /**
     * Detect nearest store and category using latitude & longitude.
     */
    public StoreInfo detectNearestStore(double latitude, double longitude) {
        String placesUrl = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=100&type=store&key=%s",
                latitude, longitude, googleApiKey
        );

        Map<String, Object> response = restTemplate.getForObject(placesUrl, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        if (results == null || results.isEmpty()) {
            return new StoreInfo("Unknown Store", "general");
        }

        Map<String, Object> firstResult = results.get(0);

        String storeName = (String) firstResult.get("name");

        // Extract Google category if available
        List<String> types = (List<String>) firstResult.get("types");
        String category = (types != null && !types.isEmpty()) ? types.get(0) : getCategoryForStore(storeName);

        return new StoreInfo(storeName, category);
    }
    /**
     * Fallback category mapping by store name keywords.
     */
    /**
     * Use Google Places Text Search to resolve store category for a given store name.
     */

/*
    public String getCategoryForStore(String storeName) {
        if (storeName == null) return "general";

        String lower = storeName.toLowerCase();
        if (lower.contains("walmart") || lower.contains("target")) return "department_store";
        if (lower.contains("amazon") || lower.contains("etsy")) return "online";
        if (lower.contains("kroger") || lower.contains("whole foods")) return "groceries";

        return "general";
    }
*/
    public String getCategoryForStore(String storeName) {
        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&key=%s",
                    storeName.replace(" ", "+"), googleApiKey
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            if (results == null || results.isEmpty()) return "general";

            // Take first match
            Map<String, Object> firstResult = results.get(0);
            List<String> types = (List<String>) firstResult.get("types");

            if (types != null && !types.isEmpty()) {
                return types.get(0); // return the first type as category
            }

            return "general";
        } catch (Exception e) {
            e.printStackTrace();
            return "general";
        }
    }
}
