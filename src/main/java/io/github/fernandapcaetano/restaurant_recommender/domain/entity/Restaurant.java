package io.github.fernandapcaetano.restaurant_recommender.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "restaurant")
public class Restaurant extends Base{

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String cuisine;

    @Column(name = "average_cost", nullable = false)
    private int averageCost;

    private double ranting;
    private int votes;

    @Column(columnDefinition = "vector(1536)", nullable = false)
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;

    public Restaurant() {
    }

    public Restaurant(String name, String city, String cuisine,
                      int averageCost, double ranting, int votes) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.averageCost = averageCost;
        this.ranting = ranting;
        this.votes = votes;
        this.embedding = null;
    }

    public void addEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCuisine() { return cuisine; }
    public int getAverageCost() { return averageCost; }
    public double getRanting() { return ranting; }
    public int getVotes() { return votes; }
    public float[] getEmbedding() { return embedding; }
}
