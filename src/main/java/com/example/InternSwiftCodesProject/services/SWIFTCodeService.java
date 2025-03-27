package com.example.InternSwiftCodesProject.services;

import com.example.InternSwiftCodesProject.SWIFTCodeRepo;
import com.example.InternSwiftCodesProject.SwiftCode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SWIFTCodeService {
    private final SWIFTCodeRepo swiftCodeRepo;
    // Constructor-based dependency injection
    public SWIFTCodeService(SWIFTCodeRepo swiftCodeRepo) {
        this.swiftCodeRepo = swiftCodeRepo;
    }
    // Map of ISO2 country codes to full country names
    private static final Map<String, String> COUNTRY_NAME_MAP = Map.ofEntries(
            // A long list of mappings from ISO2 code to country name
            // Used for validating and converting country codes
            Map.entry("AF", "AFGHANISTAN"),
            Map.entry("AL", "ALBANIA"),
            Map.entry("DZ", "ALGERIA"),
            Map.entry("AD", "ANDORRA"),
            Map.entry("AO", "ANGOLA"),
            Map.entry("AR", "ARGENTINA"),
            Map.entry("AM", "ARMENIA"),
            Map.entry("AU", "AUSTRALIA"),
            Map.entry("AT", "AUSTRIA"),
            Map.entry("AZ", "AZERBAIJAN"),
            Map.entry("BH", "BAHRAIN"),
            Map.entry("BD", "BANGLADESH"),
            Map.entry("BY", "BELARUS"),
            Map.entry("BE", "BELGIUM"),
            Map.entry("BZ", "BELIZE"),
            Map.entry("BJ", "BENIN"),
            Map.entry("BO", "BOLIVIA"),
            Map.entry("BA", "BOSNIA AND HERZEGOVINA"),
            Map.entry("BR", "BRAZIL"),
            Map.entry("BG", "BULGARIA"),
            Map.entry("CA", "CANADA"),
            Map.entry("CL", "CHILE"),
            Map.entry("CN", "CHINA"),
            Map.entry("CO", "COLOMBIA"),
            Map.entry("HR", "CROATIA"),
            Map.entry("CU", "CUBA"),
            Map.entry("CY", "CYPRUS"),
            Map.entry("CZ", "CZECH REPUBLIC"),
            Map.entry("DK", "DENMARK"),
            Map.entry("DO", "DOMINICAN REPUBLIC"),
            Map.entry("EC", "ECUADOR"),
            Map.entry("EG", "EGYPT"),
            Map.entry("EE", "ESTONIA"),
            Map.entry("FI", "FINLAND"),
            Map.entry("FR", "FRANCE"),
            Map.entry("GE", "GEORGIA"),
            Map.entry("DE", "GERMANY"),
            Map.entry("GR", "GREECE"),
            Map.entry("GT", "GUATEMALA"),
            Map.entry("HN", "HONDURAS"),
            Map.entry("HK", "HONG KONG"),
            Map.entry("HU", "HUNGARY"),
            Map.entry("IS", "ICELAND"),
            Map.entry("IN", "INDIA"),
            Map.entry("ID", "INDONESIA"),
            Map.entry("IR", "IRAN"),
            Map.entry("IE", "IRELAND"),
            Map.entry("IL", "ISRAEL"),
            Map.entry("IT", "ITALY"),
            Map.entry("JP", "JAPAN"),
            Map.entry("KZ", "KAZAKHSTAN"),
            Map.entry("KE", "KENYA"),
            Map.entry("KR", "SOUTH KOREA"),
            Map.entry("KW", "KUWAIT"),
            Map.entry("LV", "LATVIA"),
            Map.entry("LB", "LEBANON"),
            Map.entry("LT", "LITHUANIA"),
            Map.entry("LU", "LUXEMBOURG"),
            Map.entry("MY", "MALAYSIA"),
            Map.entry("MX", "MEXICO"),
            Map.entry("MA", "MOROCCO"),
            Map.entry("NL", "NETHERLANDS"),
            Map.entry("NZ", "NEW ZEALAND"),
            Map.entry("NG", "NIGERIA"),
            Map.entry("NO", "NORWAY"),
            Map.entry("PK", "PAKISTAN"),
            Map.entry("PA", "PANAMA"),
            Map.entry("PE", "PERU"),
            Map.entry("PH", "PHILIPPINES"),
            Map.entry("PL", "POLAND"),
            Map.entry("PT", "PORTUGAL"),
            Map.entry("QA", "QATAR"),
            Map.entry("RO", "ROMANIA"),
            Map.entry("RU", "RUSSIA"),
            Map.entry("SA", "SAUDI ARABIA"),
            Map.entry("RS", "SERBIA"),
            Map.entry("SG", "SINGAPORE"),
            Map.entry("SK", "SLOVAKIA"),
            Map.entry("SI", "SLOVENIA"),
            Map.entry("ZA", "SOUTH AFRICA"),
            Map.entry("ES", "SPAIN"),
            Map.entry("SE", "SWEDEN"),
            Map.entry("CH", "SWITZERLAND"),
            Map.entry("TH", "THAILAND"),
            Map.entry("TN", "TUNISIA"),
            Map.entry("TR", "TURKEY"),
            Map.entry("UA", "UKRAINE"),
            Map.entry("AE", "UNITED ARAB EMIRATES"),
            Map.entry("GB", "UNITED KINGDOM"),
            Map.entry("US", "UNITED STATES"),
            Map.entry("UY", "URUGUAY"),
            Map.entry("UZ", "UZBEKISTAN"),
            Map.entry("VN", "VIETNAM"),
            Map.entry("YE", "YEMEN"),
            Map.entry("ZW", "ZIMBABWE"),
            Map.entry("MC","MONACO"),
            Map.entry("AS", "AMERICAN SAMOA"),
            Map.entry("AI", "ANGUILLA"),
            Map.entry("AQ", "ANTARCTICA"),
            Map.entry("AG", "ANTIGUA AND BARBUDA"),
            Map.entry("AW", "ARUBA"),
            Map.entry("BS", "BAHAMAS (THE)"),
            Map.entry("BB", "BARBADOS"),
            Map.entry("BM", "BERMUDA"),
            Map.entry("BT", "BHUTAN"),
            Map.entry("BQ", "BONAIRE, SINT EUSTATIUS AND SABA"),
            Map.entry("BW", "BOTSWANA"),
            Map.entry("BN", "BRUNEI DARUSSALAM"),
            Map.entry("KH", "CAMBODIA"),
            Map.entry("MT", "MALTA")

    );
    // Check if a given ISO2 country code is valid (exists in the map)
    public static boolean isValidCountryCode(String iso2) {
        return COUNTRY_NAME_MAP.containsKey(iso2);
    }

    // Get full country name from ISO2 code, or "UNKNOWN" if not found
    public static String getCorrectCountryName(String iso2) {
        return COUNTRY_NAME_MAP.getOrDefault(iso2, "UNKNOWN");
    }

    // Retrieve all SWIFT codes for a given ISO2 country code
    public List<SwiftCode> getSwiftCodesByCountry(String countryISO2) {
        return swiftCodeRepo.findByCountryISO2IgnoreCase(countryISO2);
    }

    // Retrieve all branch SWIFT codes that start with the given prefix
    public List<SwiftCode> findBranchesByPrefix(String prefix) {
        return swiftCodeRepo.findBySwiftCodeStartingWithAndHeadquarterFlagFalse(prefix);
    }

    // Find full details of a single SWIFT code (by ID)
    public Optional<SwiftCode> getSwiftCodeDetails(String swiftCode) {
        return swiftCodeRepo.findById(swiftCode.toUpperCase());
    }

    // Find all branches linked to a given headquarter SWIFT code
    public List<SwiftCode> getBranchesForHeadquarter(String headquarterSwift) {
        return swiftCodeRepo.findByHeadquarter_SwiftCode(headquarterSwift);
    }

    // Save a new SWIFT code, and handle logic related to headquarters
    public SwiftCode saveSwiftCode(SwiftCode swiftCode) {
        Map<String, String> errorResponse = new HashMap<>();
        String swiftUpper = swiftCode.getSwiftCode().toUpperCase();
        swiftCode.setSwiftCode(swiftUpper);// Normalize SWIFT code to uppercase
        System.out.println("Received SWIFT Code: " + swiftCode.getSwiftCode());
        System.out.println("Length: " + swiftCode.getSwiftCode().length());

        // Check if the SWIFT code already exists
        if (swiftCodeRepo.existsBySwiftCode(swiftUpper)) {
            errorResponse.put ("Attempted to add duplicate SWIFT code: {}", swiftUpper);
            throw new IllegalArgumentException("SWIFT code " + swiftUpper + " already exists.");
        }

        // If the code is a headquarter, link all orphan branches to it
        if (swiftCode.isHeadquarterFlag()) {
            String swiftPrefix = swiftUpper.substring(0, 8);

            // Find orphan branches with matching prefix and no headquarter assigned

            List<SwiftCode> orphanBranches = swiftCodeRepo.findByHeadquarterIsNullAndSwiftCodeStartingWith(swiftPrefix);

            for (SwiftCode branch : orphanBranches) {
                branch.setHeadquarter(swiftCode); // Link to this HQ
                swiftCodeRepo.save(branch); // Update the branch in DB
            }
        }



        swiftCodeRepo.save(swiftCode);
        return swiftCode;
    }


    // Delete a SWIFT code (and unlink branches if it's a headquarter)
    public boolean deleteSwiftCode(String swiftCode) {
        Optional<SwiftCode> optional = swiftCodeRepo.findById(swiftCode);

        if (optional.isEmpty()) {
            return false;
        }

        SwiftCode codeToDelete = optional.get();

        if (codeToDelete.isHeadquarterFlag()) {

            // Unlink all branches from this headquarter
            List<SwiftCode> branches = swiftCodeRepo.findByHeadquarter(codeToDelete);
            for (SwiftCode branch : branches) {
                branch.setHeadquarter(null);
                swiftCodeRepo.save(branch);
            }
        }

        // Delete the SWIFT code from the repository
        swiftCodeRepo.deleteById(swiftCode);
        return true;
    }
    // Check if a SWIFT code already exists in the system
    public boolean existsBySwiftCode(String swiftCode) {
        return swiftCodeRepo.existsBySwiftCode(swiftCode.toUpperCase());
    }





}
