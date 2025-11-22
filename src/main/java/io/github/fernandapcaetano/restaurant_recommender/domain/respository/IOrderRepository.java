package io.github.fernandapcaetano.restaurant_recommender.domain.respository;

import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    @Query("""
    SELECT o FROM Order o
        ORDER BY o.orderDateTime DESC
    LIMIT 5
    """)
    List<Order> findLastFiveOrderedRestaurants();

    @Modifying
    @Query(value = """
    TRUNCATE TABLE "order" RESTART IDENTITY CASCADE
    """, nativeQuery = true)
    void delete();
}
