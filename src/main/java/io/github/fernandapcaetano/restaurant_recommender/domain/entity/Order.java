package io.github.fernandapcaetano.restaurant_recommender.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"")
public class Order extends Base{
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "order_date")
    private LocalDateTime orderDateTime;

    @Column(name = "total_amount")
    private int totalAmount;

    @Column(name = "rating")
    private double rating;

    public Order(){
        super();
    }

    public Order(Long restaurantId, LocalDateTime orderDateTime, int totalAmount, double rating) {
        super();
        this.restaurantId = restaurantId;
        this.orderDateTime = orderDateTime;
        this.totalAmount = totalAmount;
        this.rating = rating;
    }

    public Long getRestaurantId() { return restaurantId; }
    public LocalDateTime getOrderDateTime() { return orderDateTime; }
    public int getTotalAmount() { return totalAmount; }
    public double getRating() { return rating; }
}
