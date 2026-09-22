package com.priceintel.repository;

import com.priceintel.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "offers")
    @Query("""
            select distinct p from Product p join p.offers o
            where lower(p.name) like lower(concat('%', :query, '%'))
              and upper(o.countryCode) = upper(:country)
            order by p.name
            """)
    List<Product> searchInMarket(@Param("query") String query, @Param("country") String country);

    @EntityGraph(attributePaths = "offers")
    @Query("""
            select distinct p from Product p join p.offers o
            where p.id = :id and upper(o.countryCode) = upper(:country)
            """)
    Optional<Product> findInMarket(@Param("id") Long id, @Param("country") String country);

    Optional<Product> findBySlug(String slug);
}
