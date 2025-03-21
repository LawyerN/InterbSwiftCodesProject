package com.example.atakiprojekt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface SWIFTCodeRepo extends JpaRepository<SwiftCode, String> {
    Optional<SwiftCode> findBySwiftCode(String swiftCode);
    List<SwiftCode> findByCountryISO2IgnoreCase(String countryISO2);
    List<SwiftCode> findByHeadquarter_SwiftCode(String headquarterSwiftCode);

    List<SwiftCode> findAll();

    boolean existsBySwiftCode(String upperCase);
}
