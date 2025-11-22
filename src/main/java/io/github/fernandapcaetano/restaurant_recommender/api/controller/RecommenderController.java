package io.github.fernandapcaetano.restaurant_recommender.api.controller;

import io.github.fernandapcaetano.restaurant_recommender.application.dto.RecommenderRestaurantResponse;
import io.github.fernandapcaetano.restaurant_recommender.application.service.LoadDataService;
import io.github.fernandapcaetano.restaurant_recommender.application.service.RecommenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommender")
public class RecommenderController {

    private final LoadDataService loadDataService;
    private final RecommenderService recommenderService;

    public RecommenderController(LoadDataService loadDataService, RecommenderService recommenderService) {
        this.loadDataService = loadDataService;
        this.recommenderService = recommenderService;
    }

    @PostMapping
    public void loadData() {
        loadDataService.execute();
    }

    @GetMapping()
    public ResponseEntity<RecommenderRestaurantResponse> recommendRestaurants() {
        var result = recommenderService.execute();
        return ResponseEntity.ok(result);
    }

}
