package com.example.InternSwiftCodesProject.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CsvUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUploadCsvSuccessfully() throws Exception {
        String content = """
                swiftCode,countryISO2,countryName,address,bankName
                ABCDPLPW,PL,POLAND,Warsaw,Bank A
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "swiftcodes.csv",
                "text/csv",
                content.getBytes()
        );

        mockMvc.perform(multipart("/upload/swift")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully uploaded"));
    }

    @Test
    void shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/upload/swift")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }


    @Test
    void shouldReturnBadRequest_whenFileIsNotCsv() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile("file", "not-a-csv.txt", "text/plain", "Some content".getBytes());

        mockMvc.perform(multipart("/upload/swift").file(txtFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only CSV files are accepted."));
    }









}
