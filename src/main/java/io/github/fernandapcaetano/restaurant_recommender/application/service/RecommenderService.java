package io.github.fernandapcaetano.restaurant_recommender.application.service;

import io.github.fernandapcaetano.restaurant_recommender.application.dto.RecommenderRestaurantResponse;
import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Order;
import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Restaurant;
import io.github.fernandapcaetano.restaurant_recommender.domain.respository.IOrderRepository;
import io.github.fernandapcaetano.restaurant_recommender.domain.respository.IRestaurantRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RecommenderService {

    private final IOrderRepository orderRepository;
    private final IRestaurantRepository restaurantRepository;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;

    public RecommenderService(IOrderRepository orderRepository, IRestaurantRepository restaurantRepository, EmbeddingModel embeddingModel, ChatClient chatClient) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.embeddingModel = embeddingModel;
        this.chatClient = chatClient;
    }

    public RecommenderRestaurantResponse execute() {
        var lastFiveOrderedRestaurants = orderRepository.findLastFiveOrderedRestaurants();

        if (lastFiveOrderedRestaurants.isEmpty())
            return new RecommenderRestaurantResponse("No orders found to base recommendations on.", null);

        var restaurantsByOrders = restaurantRepository.findAllById(
                lastFiveOrderedRestaurants.stream().map(Order::getRestaurantId).toList()
        );

        var averageCity = restaurantsByOrders.stream()
                .collect(Collectors.groupingBy(Restaurant::getCity, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        var historyString = restaurantsByOrders.stream()
                .map(r -> """
                Cuisine: %s
                Average Cost: %d
                Restaurant Rating: %.1f
                Votes: %d
                """
                        .formatted(
                                r.getCuisine(),
                                r.getAverageCost(),
                                r.getRanting(),
                                r.getVotes()
                        )
                )
                .collect(Collectors.joining("\n"));

        var recommendationPrompt = """
        Here are the restaurants the user has ordered from recently, including restaurant attributes:

        %s

        Create a structured numerical summary that represents this user's preferences.
        Respond ONLY with JSON.

        Include fields:
        - most_frequent_cuisines (array)
        - average_price (number)
        - average_restaurant_rating (number)
        - preference_characteristics (array of descriptive traits such as "affordable", "Asian food", "high rated")
        - descriptive_tags (array)
        """
        .formatted(historyString);

        var summaryResponse = chatClient.prompt()
                .system("You are an AI specialized in analyzing restaurant preferences based on past orders.")
                .user(u -> u.text(recommendationPrompt))
                .call()
                .content();

        if (summaryResponse == null || summaryResponse.isBlank())
            return new RecommenderRestaurantResponse("Could not generate recommendations based on past orders.", null);

        var summaryEmbedding = embeddingModel.embed(summaryResponse);

        var embeddingString = IntStream.range(0, summaryEmbedding.length)
                .mapToObj(i -> Float.toString(summaryEmbedding[i]))
                .collect(Collectors.joining(",", "[", "]"));

        var similarRestaurants = restaurantRepository.findTop5ByEmbeddingSimilarTo(embeddingString, averageCity);
        var cuisineFrequency = similarRestaurants.stream()
                .collect(Collectors.groupingBy(Restaurant::getCuisine,
                        Collectors.counting()
                ));

        var responseRestaurants = similarRestaurants.stream()
                .sorted(Comparator.comparingLong((Restaurant r) ->
                        cuisineFrequency.getOrDefault(r.getCuisine(), 0L)
                ).reversed())
                .map(r -> new RecommenderRestaurantResponse.RestaurantResponse(
                        r.getName(),
                        r.getCity(),
                        r.getCuisine(),
                        r.getAverageCost(),
                        r.getRanting(),
                        r.getVotes()
                ))
                .toList();

        return new RecommenderRestaurantResponse(
                "Recommending these restaurants based in your last order",
                responseRestaurants);
    }

}
