package com.example.InternSwiftCodesProject.controllers;

import com.example.InternSwiftCodesProject.DTO.SWIFTCodeDTO;
import com.example.InternSwiftCodesProject.DTO.SWIFTCodeSimpleDTO;
import com.example.InternSwiftCodesProject.DTO.SwiftCodeWithBranchesDTO;
import com.example.InternSwiftCodesProject.SwiftCode;
import com.example.InternSwiftCodesProject.services.SWIFTCodeService;
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
        swiftCode=swiftCode.trim().toUpperCase();

        if (swiftCode.length() < 8 || swiftCode.length() > 11) {
            errorResponse.put("error", "Invalid SWIFT code format");
            errorResponse.put("message", "SWIFT code should be exactly 8 to 11 characters long");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<SwiftCode> swiftCodeOptional = swiftCodeService.getSwiftCodeDetails(swiftCode);


        if (swiftCodeOptional.isEmpty()) {
            errorResponse.put("error", "SWIFT code not found");
            errorResponse.put("message", "The SWIFT code is correctly formatted but does not exist in the database.");
            errorResponse.put("expectedLength", "8 to 11 characters");
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
        countryISO2=countryISO2.toUpperCase().trim();
        List<SwiftCode> swiftCodes = swiftCodeService.getSwiftCodesByCountry (countryISO2);
        Map<String, String> errorResponse = new HashMap<>();

        if (!SWIFTCodeService.isValidCountryCode(countryISO2)) {
            errorResponse.put("error", "Invalid country code");
            errorResponse.put("message", "Country ISO2 code '" + countryISO2 + "' is not valid.");
            return ResponseEntity.badRequest().body(errorResponse);
        }


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
        String address = swiftCode.getAddress().trim();
        String bankName = swiftCode.getBankName() != null ? swiftCode.getBankName().trim() : "";

        if (swift.isEmpty() || countryISO2.isEmpty() || countryName.isEmpty() || bankName.isEmpty()) {
            errorResponse.put("error", "Missing required fields");
            errorResponse.put("message", "All fields (swiftCode, countryISO2, countryName, address, bankName) must be provided.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if(address.isEmpty()){
            address = "No address provided";
        }


        if(address.length()<3 || address.length()>500) {
            errorResponse.put("error", "Address must be between 3 and 500 characters");
            return ResponseEntity.badRequest().body(errorResponse);

        }

        // Validate SWIFT code length
        if (swift.length() <8 || swift.length() >11) {
            errorResponse.put("error", "Invalid SWIFT code format");
            errorResponse.put("message", "SWIFT code should be exactly between 8 and 11 characters. ");
            errorResponse.put("providedSwiftCode", swift);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (!swift.matches("^[A-Za-z0-9]+$")) {
            errorResponse.put("error", "Invalid characters in SWIFT code");
            errorResponse.put("message", "SWIFT code must contain only  letters A-Z(a-z) and digits 0-9.");
            return ResponseEntity.badRequest().body(errorResponse);
        }


        if(swiftCodeService.existsBySwiftCode(swift)) {
            errorResponse.put("error", "Duplicate SWIFT code");
            errorResponse.put("message", "SWIFT code " + swift + " already exists in the database.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        if (!SWIFTCodeService.isValidCountryCode(countryISO2)) {
            errorResponse.put("error", "Invalid country code");
            errorResponse.put("message", "Country ISO2 code '" + countryISO2 + "' is not valid.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        String correctCountryName = SWIFTCodeService.getCorrectCountryName(countryISO2);
        if (!countryName.equals(correctCountryName)) {
            errorResponse.put("error", "Country name mismatch");
            errorResponse.put("message", "The provided country name '" + countryName + "' does not match the expected name '" + correctCountryName + "'.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Determine if this is a headquarter
        boolean isHeadquarter = swift.endsWith("XXX");
        String swiftPrefix = swift.substring(0, 8);
        String expectedHQCode = swiftPrefix + "XXX";

        // Jeśli branch → sprawdź czy HQ istnieje
        //Optional<SwiftCode> hq = swiftCodeService.getSwiftCodeDetails(expectedHQCode);
        //hq.ifPresent(swiftCode::setHeadquarter);

        // Set fields
        swiftCode.setSwiftCode(swift);
        swiftCode.setCountryISO2(countryISO2);
        swiftCode.setCountryName(correctCountryName);
        swiftCode.setHeadquarterFlag(isHeadquarter);
        swiftCode.setAddress(address);
        swiftCode.setBankName(bankName);

        // Save SWIFT Code
        if (!isHeadquarter) {
            //SwiftCode savedHQ = swiftCodeService.saveSwiftCode(swiftCode);
            // Branch is allowed to be saved as orphan (without HQ)
            // Optionally: you could search for HQ and assign if exists, but not required
            Optional<SwiftCode> hq = swiftCodeService.getSwiftCodeDetails(expectedHQCode);
            hq.ifPresent(swiftCode::setHeadquarter);

            swiftCodeService.saveSwiftCode(swiftCode);
            return ResponseEntity.ok(Map.of("message", "Branch added. Linked to HQ if exists."));
        }

        // === CASE 2: Headquarter ===
        // First, save the HQ itself (so it gets ID)
        SwiftCode savedHQ = swiftCodeService.saveSwiftCode(swiftCode);
        // Now update all orphan branches
        List<SwiftCode> orphanBranches = swiftCodeService.findBranchesByPrefix(swiftPrefix);
        for (SwiftCode branch : orphanBranches) {
            if (branch.getHeadquarter() == null) {
                branch.setHeadquarter(savedHQ);
                swiftCodeService.saveSwiftCode(branch);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Headquarter added. Linked orphan branches if found."));
    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<Map<String,String>> deleteSwiftCode(@PathVariable String swiftCode) {
        swiftCode = swiftCode.trim().toUpperCase();
        Map<String, String> response = new HashMap<>();
        Optional<SwiftCode> optional = swiftCodeService.getSwiftCodeDetails(swiftCode);
        if (optional.isEmpty()) {
            response.put("error", "SWIFT code not found");
            response.put("message", "No such SWIFT code exists in the database: " + swiftCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        boolean isHQ = optional.get().isHeadquarterFlag();
        List<SwiftCode> linkedBranches = new ArrayList<>();
        if (isHQ) {
            linkedBranches = swiftCodeService.getBranchesForHeadquarter(swiftCode);
        }
        boolean deleted = swiftCodeService.deleteSwiftCode(swiftCode);


        if (deleted) {
            if(isHQ) {
                response.put("message", "Headquarter deleted. " +
                        (linkedBranches.isEmpty()
                                ? "No branches were linked."
                                : linkedBranches.size() + " branch(es) are now orphaned."));
            }else {
                response.put("message", "Branch deleted successfully.");
            }
            response.put("swiftCode", swiftCode);
            return ResponseEntity.ok(response);

        }

        response.put("error", "Unexpected error during deletion.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }





}
