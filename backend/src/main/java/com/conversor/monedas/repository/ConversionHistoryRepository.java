package com.conversor.monedas.repository;

import com.conversor.monedas.model.ConversionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    Page<ConversionHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
