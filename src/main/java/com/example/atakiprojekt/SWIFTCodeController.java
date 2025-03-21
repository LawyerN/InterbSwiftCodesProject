package com.example.atakiprojekt;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class SWIFTCodeController {
    private final SWIFTCodeService swiftCodeService;
    public SWIFTCodeController(SWIFTCodeService swiftCodeService) {
        this.swiftCodeService = swiftCodeService;
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<?> getSwiftCodeDetails(@PathVariable String swiftCode) {
        Map<String, String> errorResponse = new HashMap<>();

        if (swiftCode.length() != 8 && swiftCode.length() != 11) {
            errorResponse.put("error", "Invalid SWIFT code format");
            errorResponse.put("message", "SWIFT code should be exactly 8 or 11 characters long");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<SwiftCode> swiftCodeOptional = swiftCodeService.getSwiftCodeDetails(swiftCode);


        if (swiftCodeOptional.isEmpty()) {
            errorResponse.put("error", "SWIFT code not found");
            errorResponse.put("message", "The SWIFT code is correctly formatted but does not exist in the database.");
            errorResponse.put("expectedLength", "8 or 11 characters");
            errorResponse.put("providedSwiftCode", swiftCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
//        if (swiftCodeOptional.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }

        SwiftCode swiftCodeDetails = swiftCodeOptional.get();

        if (swiftCodeDetails.isHeadquarterFlag()) {
            List<SwiftCode> branchesList = swiftCodeService.getBranchesForHeadquarter(swiftCode);
            List<SWIFTCodeDTO> branches = new ArrayList<>();

            for (SwiftCode branch : branchesList) {
                branches.add(new SWIFTCodeDTO(
                        branch.getAddress(),
                        branch.getBankName(),
                        branch.getCountryISO2(),
                        branch.getCountryName(),
                        branch.isHeadquarterFlag(),
                        branch.getSwiftCode()
                ));
            }

            SwiftCodeWithBranchesDTO response = new SwiftCodeWithBranchesDTO(
                    swiftCodeDetails.getAddress(),
                    swiftCodeDetails.getBankName(),
                    swiftCodeDetails.getCountryISO2(),
                    swiftCodeDetails.getCountryName(),
                    swiftCodeDetails.isHeadquarterFlag(),
                    swiftCodeDetails.getSwiftCode(),
                    branches
            );

            return ResponseEntity.ok(response);
        }

        SWIFTCodeDTO response = new SWIFTCodeDTO(
                swiftCodeDetails.getAddress(),
                swiftCodeDetails.getBankName(),
                swiftCodeDetails.getCountryISO2(),
                swiftCodeDetails.getCountryName(),
                swiftCodeDetails.isHeadquarterFlag(),
                swiftCodeDetails.getSwiftCode()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryISO2}")
    public ResponseEntity<?> getSwiftCodesByCountry(@PathVariable String countryISO2) {
        List<SwiftCode> swiftCodes = swiftCodeService.getSwiftCodesByCountry (countryISO2);

        if (swiftCodes.isEmpty()) {
            System.out.println("Error: Not Found");
            return ResponseEntity.notFound().build();
        }
        String countryName = swiftCodes.get(0).getCountryName();

        List<SWIFTCodeSimpleDTO> swiftCodeDTOs = new ArrayList<>();

        for (SwiftCode swiftCode : swiftCodes) {
            swiftCodeDTOs.add(new SWIFTCodeSimpleDTO(
                    swiftCode.getAddress(),
                    swiftCode.getBankName(),
                    swiftCode.getCountryISO2(),
                    swiftCode.isHeadquarterFlag(),
                    swiftCode.getSwiftCode()
            ));
        }


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("countryISO2", countryISO2.toUpperCase());
        response.put("countryName", countryName);
        response.put("swiftCodes", swiftCodeDTOs);


        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addSwiftCode( @RequestBody SwiftCode swiftCode) {
        Map<String, String> errorResponse = new HashMap<>();

        // Normalize input
        String swift = swiftCode.getSwiftCode().trim().toUpperCase();
        String countryISO2 = swiftCode.getCountryISO2().trim().toUpperCase();
        String countryName = swiftCode.getCountryName().trim().toUpperCase();

        // Validate SWIFT code length
        if (swift.length() != 8 && swift.length() != 11) {
            errorResponse.put("error", "Invalid SWIFT code format");
            errorResponse.put("message", "SWIFT code should be exactly 8 or 11 characters long.");
            errorResponse.put("providedSwiftCode", swift);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Determine if this is a headquarter
        boolean isHeadquarter = swift.endsWith("XXX");
        String swiftPrefix = swift.substring(0, 8);
        String expectedHQCode = swiftPrefix + "XXX";

        // Jeśli branch → sprawdź czy HQ istnieje
        if (!isHeadquarter) {
            Optional<SwiftCode> hq = swiftCodeService.getSwiftCodeDetails(expectedHQCode);

                if (hq.isEmpty()) {
                    errorResponse.put("error", "Missing headquarter");
                    errorResponse.put("message", "Cannot add branch without existing headquarter: " + expectedHQCode);
                    return ResponseEntity.badRequest().body(errorResponse);
                }

            swiftCode.setHeadquarter(hq.get());
        }

        // Set fields
        swiftCode.setSwiftCode(swift);
        swiftCode.setCountryISO2(countryISO2);
        swiftCode.setCountryName(countryName);
        swiftCode.setHeadquarterFlag(isHeadquarter);

        // Save SWIFT Code
        swiftCodeService.saveSwiftCode(swiftCode);

        return ResponseEntity.ok(Map.of("message", "SWIFT Code added successfully."));

    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<String> deleteSwiftCode(@PathVariable String swiftCode) {
        boolean deleted = swiftCodeService.deleteSwiftCode(swiftCode);
        if (deleted) {
            return ResponseEntity.ok("SWIFT Code deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }





}
