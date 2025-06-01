package com.invoice.backend.infrastructure.client.repository;

import com.invoice.backend.entity.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Client> searchClients(@Param("query") String query);

    List<Client> findByUserId(Long userId);
}
