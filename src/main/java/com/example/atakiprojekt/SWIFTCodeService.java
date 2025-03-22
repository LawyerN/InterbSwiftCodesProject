package com.example.atakiprojekt;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SWIFTCodeService {
    private final SWIFTCodeRepo swiftCodeRepo;

    private static final Map<String, String> COUNTRY_NAME_MAP = Map.ofEntries(
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
            Map.entry("ZW", "ZIMBABWE")
    );

    public static boolean isValidCountryCode(String iso2) {
        return COUNTRY_NAME_MAP.containsKey(iso2);
    }

    public static String getCorrectCountryName(String iso2) {
        return COUNTRY_NAME_MAP.getOrDefault(iso2, "UNKNOWN");
    }

    public SWIFTCodeService(SWIFTCodeRepo swiftCodeRepo) {
        this.swiftCodeRepo = swiftCodeRepo;
    }
    public List<SwiftCode> getSwiftCodesByCountry(String countryISO2) {
        return swiftCodeRepo.findByCountryISO2IgnoreCase(countryISO2);
    }

    public List<SwiftCode> findBranchesByPrefix(String prefix) {
        return swiftCodeRepo.findBySwiftCodeStartingWithAndHeadquarterFlagFalse(prefix);
    }


    public Optional<SwiftCode> getSwiftCodeDetails(String swiftCode) {
        return swiftCodeRepo.findById(swiftCode.toUpperCase());
    }

    public List<SwiftCode> getBranchesForHeadquarter(String headquarterSwift) {
        return swiftCodeRepo.findByHeadquarter_SwiftCode(headquarterSwift);
    }

    public SwiftCode saveSwiftCode(SwiftCode swiftCode) {
        Map<String, String> errorResponse = new HashMap<>();
        String swiftUpper = swiftCode.getSwiftCode().toUpperCase();
        swiftCode.setSwiftCode(swiftUpper);
        System.out.println("Received SWIFT Code: " + swiftCode.getSwiftCode());
        System.out.println("Length: " + swiftCode.getSwiftCode().length());
        //String swiftUpper = swiftCode.getSwiftCode().toUpperCase();
        if (swiftCodeRepo.existsBySwiftCode(swiftUpper)) {
            errorResponse.put ("Attempted to add duplicate SWIFT code: {}", swiftUpper);
            throw new IllegalArgumentException("SWIFT code " + swiftUpper + " already exists.");
        }
        if (swiftCode.isHeadquarterFlag()) {
            String swiftPrefix = swiftUpper.substring(0, 8);

            // Szukamy osieroconych branchy bez przypisanego HQ
            List<SwiftCode> orphanBranches = swiftCodeRepo.findByHeadquarterIsNullAndSwiftCodeStartingWith(swiftPrefix);

            for (SwiftCode branch : orphanBranches) {
                branch.setHeadquarter(swiftCode); // Przypisujemy nowo dodanemu HQ
                swiftCodeRepo.save(branch); // Aktualizujemy w bazie
            }
        }



        swiftCodeRepo.save(swiftCode);
        return swiftCode;
    }

    public boolean deleteSwiftCode(String swiftCode) {
        Optional<SwiftCode> optional = swiftCodeRepo.findById(swiftCode);

        if (optional.isEmpty()) {
            return false;
        }

        SwiftCode codeToDelete = optional.get();

        if (codeToDelete.isHeadquarterFlag()) {
            // Odłącz branche
            List<SwiftCode> branches = swiftCodeRepo.findByHeadquarter(codeToDelete);
            for (SwiftCode branch : branches) {
                branch.setHeadquarter(null);
                swiftCodeRepo.save(branch);
            }
        }

        swiftCodeRepo.deleteById(swiftCode);
        return true;
    }
    public boolean existsBySwiftCode(String swiftCode) {
        return swiftCodeRepo.existsBySwiftCode(swiftCode.toUpperCase());
    }



}
