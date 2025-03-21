package com.example.atakiprojekt;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SWIFTCodeService {
    private final SWIFTCodeRepo swiftCodeRepo;

    public SWIFTCodeService(SWIFTCodeRepo swiftCodeRepo) {
        this.swiftCodeRepo = swiftCodeRepo;
    }
    public List<SwiftCode> getSwiftCodesByCountry(String countryISO2) {
        return swiftCodeRepo.findByCountryISO2IgnoreCase(countryISO2);
    }


    public Optional<SwiftCode> getSwiftCodeDetails(String swiftCode) {
        return swiftCodeRepo.findById(swiftCode);
    }

    public List<SwiftCode> getBranchesForHeadquarter(String headquarterSwift) {
        return swiftCodeRepo.findByHeadquarter_SwiftCode(headquarterSwift);
    }

    public void saveSwiftCode(SwiftCode swiftCode) {
        System.out.println("Received SWIFT Code: " + swiftCode.getSwiftCode());
        System.out.println("Length: " + swiftCode.getSwiftCode().length());
        swiftCodeRepo.save(swiftCode);
    }

    public boolean deleteSwiftCode(String swiftCode) {
        if (swiftCodeRepo.existsById(swiftCode)) {
            swiftCodeRepo.deleteById(swiftCode);
            return true;
        }
        return false;
    }
    public boolean existsBySwiftCode(String swiftCode) {
        return swiftCodeRepo.existsBySwiftCode(swiftCode.toUpperCase());
    }



}
