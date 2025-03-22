package com.example.atakiprojekt;

import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CSVParserService {
    private final SWIFTCodeRepo repository;
    @Autowired
    private SWIFTCodeController swiftCodeController;


    public CSVParserService(SWIFTCodeRepo repository) {
        this.repository = repository;
    }
//    File file = new File("src/main/resources/test.csv");
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data/test.csv");
            InputStream inputStream = resource.getInputStream();
            System.out.println("📥 Importing SWIFT data on startup...");
            parseAndStoreSwiftData(inputStream);
            System.out.println("✅ SWIFT data imported!");
        } catch (Exception e) {
            System.err.println("❌ Error while loading default SWIFT codes: " + e.getMessage());
        }
    }


//    public void parseAndStoreSwiftData(InputStream file) {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
//
//            CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withSkipHeaderRecord());
//            List<SwiftCode> existingSwiftCodes = repository.findAll(); // Pobranie istniejących kodów SWIFT
//            Set<String> processedSwiftCodes = new HashSet<>();
//            Map<String, SwiftCode> headquartersMap = new HashMap<>(); //creating map for headquarters
//
//            for (CSVRecord record : csvParser) {  //going through the csv  and getting data frrom it
//                String swiftCode = record.get("SWIFT CODE").trim();
//                if (existingSwiftCodes.contains(swiftCode) || processedSwiftCodes.contains(swiftCode)) {
//                    System.out.println("Error: duplicated SWIFT code (skipping): " + swiftCode);
//                    continue;
//                }
//                String bankName = record.get("NAME").trim();
//                String address = record.get("ADDRESS").trim();
//                if (address != null) {
//                    address = address.replaceAll("[^\\x20-\\x7E]", "").trim();
//                } else {
//                    address = "Brak adresu";
//                }
//                String countryISO2 = record.get("COUNTRY ISO2 CODE").trim().toUpperCase();
//                String countryName = record.get("COUNTRY NAME").trim().toUpperCase();
//
//                boolean isHeadquarter = swiftCode.endsWith("XXX"); //so if it ends with XXX then it is headquarter and we save it as a boolean
//
//                SwiftCode swiftEntry = new SwiftCode(
//                        swiftCode,
//                        bankName,
//                        address,
//                        countryISO2,
//                        countryName,
//                        isHeadquarter
//
//                );
//                System.out.println("Saving to database: " + swiftCode + " | " + address); // Debugging
//
//                if (isHeadquarter) {
//                    headquartersMap.put(swiftCode.substring(0, 8), swiftEntry); //if it is a headquarter then we save first 8 chars into a hashmap
//                }
//
//                repository.save(swiftEntry); //saving
//            }
//
//            for (SwiftCode branch : repository.findAll()) { //we want to get every bank
//                if (!branch.isHeadquarterFlag()) { //if it is not a headquarter then we try to find a headquarter in headquartersMap
//                    SwiftCode headquarters = headquartersMap.get(branch.getSwiftCode().substring(0, 8));
//                    if (headquarters != null) {
//                        branch.setHeadquarter(headquarters); //If we find matching headquarter then we assign it to the branch
//                        repository.save(branch); //then we save it into the db
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            System.err.println("error" + e.getMessage());
//        }
//    }

    public void parseAndStoreSwiftData(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            int added = 0;
            int skipped = 0;

            for (CSVRecord record : csvParser) {
                SwiftCode swiftCode = new SwiftCode(
                        record.get("SWIFT CODE").trim(),
                        record.get("NAME").trim(),
                        record.get("ADDRESS").trim(),
                        record.get("COUNTRY ISO2 CODE").trim().toUpperCase(),
                        record.get("COUNTRY NAME").trim().toUpperCase(),
                        record.get("SWIFT CODE").trim().endsWith("XXX")
                );

                ResponseEntity<Map<String, String>> response = swiftCodeController.addSwiftCode(swiftCode);
                if (response.getStatusCode().is2xxSuccessful()) {
                    added++;
                } else {
                    skipped++;
                    System.out.println("Skipped SWIFT: " + swiftCode.getSwiftCode() + " Reason: " + Objects.requireNonNull(response.getBody()).get("message"));
                }
            }

            System.out.println("Import complete: " + added + " added, " + skipped + " skipped.");

        } catch (Exception e) {
            System.err.println("Error during import: " + e.getMessage());
        }
    }


}
