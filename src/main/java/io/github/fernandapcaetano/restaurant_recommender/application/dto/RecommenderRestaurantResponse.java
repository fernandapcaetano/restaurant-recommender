package io.github.fernandapcaetano.restaurant_recommender.application.dto;

import java.util.List;

public record RecommenderRestaurantResponse(
        String message,
        List<RestaurantResponse> restaurant
) {
    public record RestaurantResponse(
            String name,
            String city,
            String cuisine,
            int averageCost,
            double rating,
            int votes
    ) {}
}
