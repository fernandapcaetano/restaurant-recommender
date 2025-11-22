package io.github.fernandapcaetano.restaurant_recommender.domain.respository;

import io.github.fernandapcaetano.restaurant_recommender.domain.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query(value = """
    SELECT * FROM restaurant
    WHERE city = :city
    ORDER BY embedding <-> CAST(:queryVector AS vector) LIMIT 5
    """, nativeQuery = true)
    List<Restaurant> findTop5ByEmbeddingSimilarTo(@Param("queryVector") String queryVector,
                                                  @Param("city") String city);

    @Modifying
    @Query(value = """
    TRUNCATE TABLE restaurant RESTART IDENTITY CASCADE
    """, nativeQuery = true)
    void delete();
}
