package io.github.fernandapcaetano.restaurant_recommender.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Summary information about a restaurant.")
public record SummaryRestaurantResponse(
        @Schema(description = "City of the restaurant.")
        String city,
        @Schema(description = "Array Cuisine type of the restaurant.")
        String[] cuisine,
        @Schema(description = "Average cost at the restaurant.")
        int averageCost,
        @Schema(description = "Rating of the restaurant.")
        double rating,
        @Schema(description = "Number of votes the restaurant has received.")
        int votes
) {
}
