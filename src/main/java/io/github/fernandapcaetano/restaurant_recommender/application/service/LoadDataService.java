package io.github.fernandapcaetano.restaurant_recommender.application.service;

import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Order;
import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Restaurant;
import io.github.fernandapcaetano.restaurant_recommender.domain.respository.IOrderRepository;
import io.github.fernandapcaetano.restaurant_recommender.domain.respository.IRestaurantRepository;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class LoadDataService {

    @Value("${data.restaurant.path}")
    private Resource dataRestaurantPath;
    @Value("${data.order.path}")
    private Resource dataOrderPath;
    private final IRestaurantRepository restaurantRepository;
    private final IOrderRepository orderRepository;
    private final EmbeddingModel embeddingModel;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDataService.class);

    public LoadDataService(IRestaurantRepository restaurantRepository, IOrderRepository orderRepository, EmbeddingModel embeddingModel) {
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public void execute() {
        try {
            LOGGER.info("Clearing existing data...");
            orderRepository.delete();
            restaurantRepository.delete();
            LOGGER.info("Existing data cleared.");
            var restaurants = loadRestaurantData();
            LOGGER.info("Generating embeddings for restaurant data...");
            var embeddings = restaurants.parallelStream()
                    .peek(restaurant -> {
                        var name = restaurant.getName();
                        var cuisine = restaurant.getCuisine();
                        var averageCost = restaurant.getAverageCost();
                        var rating = restaurant.getRanting();
                        var votes = restaurant.getVotes();
                        var toEmbed = "Name: %s; Cuisine: %s; Average Cost: %d; Rating: %.1f; Votes: %d"
                                .formatted(name, cuisine, averageCost, rating, votes);
                        var embedding = embeddingModel.embed(toEmbed);
                        restaurant.addEmbedding(embedding);
                    })
                    .toList();
            LOGGER.info("Saving restaurant data to the database...");
            var savedRestaurants = restaurantRepository.saveAll(embeddings);
            var restaurantsId = savedRestaurants.stream().map(Restaurant::getId).toArray(Long[]::new);
            LOGGER.info("Loading order data for the saved restaurants...");
            var orders = loadOrderData(restaurantsId);
            LOGGER.info("Saving order data to the database...");
            orderRepository.saveAll(orders);
            LOGGER.info("Data loading completed successfully.");
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
        }
    }

    public List<Restaurant> loadRestaurantData() throws Exception {
        LOGGER.info("Loading restaurant data from path: {}", dataRestaurantPath);
        var csvFile = dataRestaurantPath.getInputStream();

        try (var reader = new InputStreamReader(csvFile, StandardCharsets.ISO_8859_1)) {
            var registers = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setDelimiter(',')
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .get()
                    .parse(reader)
                    .stream();

            var restaurants = registers
            .filter(record ->
                    !record.get("Country Code").isBlank() && Integer.parseInt(record.get("Country Code")) == 30
            )
            .map(record -> {
                var name = record.get("Restaurant Name");
                var city = record.get("City");
                var cuisine = record.get("Cuisines");
                var averageCost = Integer.parseInt(record.get("Average Cost for two"));
                var rating = Double.parseDouble(record.get("Aggregate rating"));
                var votes = Integer.parseInt(record.get("Votes"));
                return new Restaurant(name, city, cuisine, averageCost, rating, votes);
            }).toList();

            LOGGER.info("Restaurant data loaded successfully.");
            return restaurants;
        }
    }
    public List<Order> loadOrderData(Long[] restaurantsId) throws Exception{
        LOGGER.info("Loading order data from path: {}", dataOrderPath);
        var csvFile = dataOrderPath.getInputStream();

        try (var reader = new InputStreamReader(csvFile, StandardCharsets.ISO_8859_1)) {
            var registers = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setDelimiter(',')
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .get()
                    .parse(reader)
                    .stream();

            var orders = registers
                    .filter(record -> {
                        var restaurantId = Long.parseLong(record.get("Restaurant ID"));
                        for (Long id : restaurantsId) {
                            if (restaurantId == id) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .map(record -> {
                        var restaurantId = Long.parseLong(record.get("Restaurant ID"));
                        if (restaurantId > 30L)
                            return null;
                        var orderDateTime = LocalDateTime.parse(record.get("Order Date"), DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"));
                        var totalAmount = Integer.parseInt(record.get("Order Amount"));
                        var rating = Double.parseDouble(record.get("Customer Rating-Food"));
                        return new Order(restaurantId, orderDateTime, totalAmount, rating);
                    })
                    .filter(Objects::nonNull)
                    .limit(20)
                    .toList();

            LOGGER.info("Order data loaded successfully.");
            return orders;
        }
    }
}
