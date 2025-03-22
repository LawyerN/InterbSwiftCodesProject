package com.example.atakiprojekt;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController


@RequestMapping("/upload")
public class CsvUploadController {
    private final CSVParserService csvParserService;

    public CsvUploadController(CSVParserService csvParserService) {
        this.csvParserService = csvParserService;
    }

    @PostMapping("/swift")
        public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try {
            csvParserService.parseAndStoreSwiftData(file.getInputStream());
            return ResponseEntity.ok("Successfully uploaded");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
