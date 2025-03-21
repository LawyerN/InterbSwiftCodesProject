package com.example.atakiprojekt;

import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class CSVParserService {
    private final SWIFTCodeRepo repository;


    public CSVParserService(SWIFTCodeRepo repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void loadCSVFromFile() {
        Path filePath = Paths.get("src/main/resources/test.csv");
        if (Files.exists(filePath)) {
            try {
                System.out.printf("Loading data from file %s%n", filePath);
                MultipartFile multipartFile = new MockMultipartFile("test.csv", Files.readAllBytes(filePath));
                parseAndStoreSwiftData(multipartFile);
                System.out.printf("The file %s has been loaded %n", filePath);
            } catch (Exception e) {
                System.err.printf("Error loading data from file %s%n", filePath);
            }
        } else {
            System.out.printf("File %s does not exist, an app will start without data you can add it with postman");
        }
    }


    public void parseAndStoreSwiftData(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withSkipHeaderRecord());

            Map<String, SwiftCode> headquartersMap = new HashMap<>(); //creating map for headquarters

            for (CSVRecord record : csvParser) {  //going through the csv  and getting data frrom it
                String swiftCode = record.get("SWIFT CODE").trim();
                if (repository.existsById(swiftCode)) {
                    System.out.println("Error: duplicated SWIFT code: " + swiftCode);
                    continue;
                }
                String bankName = record.get("NAME").trim();
                String address = record.get("ADDRESS").trim();
                if (address != null) {
                    address = address.replaceAll("[^\\x20-\\x7E]", "").trim();
                } else {
                    address = "Brak adresu";
                }
                String countryISO2 = record.get("COUNTRY ISO2 CODE").trim().toUpperCase();
                String countryName = record.get("COUNTRY NAME").trim().toUpperCase();

                boolean isHeadquarter = swiftCode.endsWith("XXX"); //so if it ends with XXX then it is headquarter and we save it as a boolean

                SwiftCode swiftEntry = new SwiftCode(
                        swiftCode,
                        bankName,
                        address,
                        countryISO2,
                        countryName,
                        isHeadquarter

                );
                System.out.println("Saving to database: " + swiftCode + " | " + address); // Debugging

                if (isHeadquarter) {
                    headquartersMap.put(swiftCode.substring(0, 8), swiftEntry); //if it is a headquarter then we save first 8 chars into a hashmap
                }

                repository.save(swiftEntry); //saving
            }

            for (SwiftCode branch : repository.findAll()) { //we want to get every bank
                if (!branch.isHeadquarterFlag()) { //if it is not a headquarter then we try to find a headquarter in headquartersMap
                    SwiftCode headquarters = headquartersMap.get(branch.getSwiftCode().substring(0, 8));
                    if (headquarters != null) {
                        branch.setHeadquarter(headquarters); //If we find matching headquarter then we assign it to the branch
                        repository.save(branch); //then we save it into the db
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("error" + e.getMessage());
        }
    }
}
