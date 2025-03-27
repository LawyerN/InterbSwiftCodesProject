package com.example.InternSwiftCodesProject.services;

import com.example.InternSwiftCodesProject.controllers.SWIFTCodeController;
import com.example.InternSwiftCodesProject.SWIFTCodeRepo;
import com.example.InternSwiftCodesProject.SwiftCode;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class CSVParserService {
    private final SWIFTCodeRepo repository;

    private SWIFTCodeController swiftCodeController;


    // Constructor injection of dependencies
    public CSVParserService(SWIFTCodeRepo repository, SWIFTCodeController swiftCodeController) {
        this.swiftCodeController = swiftCodeController;
        this.repository = repository;
    }
    // This method is automatically called after the service is initialized
    @PostConstruct
    public void init() {
        try {
            // Resolve all CSV files located in the classpath under 'data/' folder
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/*.csv");

            // Loop through each found resource (CSV file)
            for (Resource resource : resources) {
                // Skip files that don't have .csv extension
                if (!Objects.requireNonNull(resource.getFilename()).endsWith(".csv")) {

                }
                // Try to open and parse each file
                try (InputStream inputStream = resource.getInputStream()) {
                    System.out.println("Processing: " + resource.getFilename());
                    parseAndStoreSwiftData(inputStream);
                } catch (Exception e) {
                    System.err.println("Error reading file: " + resource.getFilename() + " - " + e.getMessage());
                }
            }
            System.out.println("All CSVs processed.");

        } catch (Exception e) {
            System.err.println("Error while loading default SWIFT codes: " + e.getMessage());
        }
    }



    // Parses a single CSV input stream and stores the data via the controller
    public void parseAndStoreSwiftData(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            // Use Apache Commons CSV to parse the data with headers
            CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            int added = 0; // Track successfully added records
            int skipped = 0;// Track skipped records

            // Iterate over each CSV record
            for (CSVRecord record : csvParser) {
                // Create a SwiftCode object from CSV data
                SwiftCode swiftCode = new SwiftCode(
                        record.get("SWIFT CODE").trim(),
                        record.get("NAME").trim(),
                        record.get("ADDRESS").trim(),
                        record.get("COUNTRY ISO2 CODE").trim().toUpperCase(),
                        record.get("COUNTRY NAME").trim().toUpperCase(),
                        record.get("SWIFT CODE").trim().endsWith("XXX")
                );

                // Try to add the SWIFT code using the controller
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
