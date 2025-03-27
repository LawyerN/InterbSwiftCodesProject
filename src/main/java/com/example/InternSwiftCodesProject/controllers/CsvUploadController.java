package com.example.InternSwiftCodesProject.controllers;

import com.example.InternSwiftCodesProject.services.CSVParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@RestController


@RequestMapping("/upload")
public class CsvUploadController {
    private final CSVParserService csvParserService;
    // Constructor-based dependency injection
    public CsvUploadController(CSVParserService csvParserService) {
        this.csvParserService = csvParserService;
    }

    @PostMapping("/swift")
        public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {

        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        // Check if the uploaded file has a .csv extension
        if(!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Only CSV files are accepted.");
        }
        try {
            // Pass the file input stream to the parser service
            csvParserService.parseAndStoreSwiftData(file.getInputStream());
            return ResponseEntity.ok("Successfully uploaded");
        }catch (Exception e) {
            // Return the error message if something goes wrong during parsing
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
