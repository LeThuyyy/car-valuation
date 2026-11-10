package com.carvaluation.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferenceCarPriceRepository extends JpaRepository<ReferenceCarPrice, Long> {
    Optional<ReferenceCarPrice> findFirstByMakeIgnoreCaseAndModelIgnoreCaseAndYear(String make, String model, Integer year);
    List<ReferenceCarPrice> findByMakeIgnoreCaseOrderByModelAscYearDesc(String make);
    List<ReferenceCarPrice> findByMakeIgnoreCaseAndModelIgnoreCaseOrderByYearDesc(String make, String model);
}
