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



    public CSVParserService(SWIFTCodeRepo repository, SWIFTCodeController swiftCodeController) {
        this.swiftCodeController = swiftCodeController;
        this.repository = repository;
    }
//    File file = new File("src/main/resources/test.csv");
    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/*.csv");


            for (Resource resource : resources) {
                if (!Objects.requireNonNull(resource.getFilename()).endsWith(".csv")) {

                }
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
