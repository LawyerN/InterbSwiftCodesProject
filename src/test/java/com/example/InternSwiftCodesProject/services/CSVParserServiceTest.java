package com.example.InternSwiftCodesProject.services;

import com.example.InternSwiftCodesProject.SWIFTCodeRepo;
import com.example.InternSwiftCodesProject.SwiftCode;
import com.example.InternSwiftCodesProject.controllers.SWIFTCodeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class CSVParserServiceTest {

    @Mock
    private SWIFTCodeRepo repository;

    @Mock
    private SWIFTCodeController swiftCodeController;

    @InjectMocks
    private CSVParserService csvParserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void parseAndStoreSwiftData_shouldAddSingleValidSwiftCode() {
        // Given: sample CSV data with 1 valid row
        String csvData = """
                SWIFT CODE,NAME,ADDRESS,COUNTRY ISO2 CODE,COUNTRY NAME
                BANKPLPWXXX,Bank Polska,Main St 1,PL,POLAND
                """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        // Mock controller to always return 200 OK
        when(swiftCodeController.addSwiftCode(any(SwiftCode.class)))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Added")));

        // When
        csvParserService.parseAndStoreSwiftData(inputStream);

        // Then
        verify(swiftCodeController, times(1)).addSwiftCode(any(SwiftCode.class));
    }

    @Test
    void parseAndStoreSwiftData_shouldSkipInvalidSwiftCode() {
        // Given: sample CSV data with 1 valid and 1 invalid row
        String csvData = """
                SWIFT CODE,NAME,ADDRESS,COUNTRY ISO2 CODE,COUNTRY NAME
                BANKPLPWXXX,Bank Polska,Main St 1,PL,POLAND
                DUPLICATE,Bank XYZ,Address 2,XX,UNKNOWN
                """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        // Mock valid response for first row
        when(swiftCodeController.addSwiftCode(argThat(sw -> sw != null && "BANKPLPWXXX".equals(sw.getSwiftCode()))))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Added")));
        // Mock error response for invalid/duplicate row
        when(swiftCodeController.addSwiftCode(argThat(sw -> sw != null && "DUPLICATE".equals(sw.getSwiftCode()))))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Duplicate")));

        // When
        csvParserService.parseAndStoreSwiftData(inputStream);

        // Then
        verify(swiftCodeController, times(2)).addSwiftCode(any(SwiftCode.class));
    }




    @Test
    void parseAndStoreSwiftData_shouldAddMultipleValidRecords() {
        // Should add 3 valid SWIFT records from CSV
        String csvData = """
            SWIFT CODE,NAME,ADDRESS,COUNTRY ISO2 CODE,COUNTRY NAME
            BANKPLPWXXX,Bank 1,Main St 1,PL,POLAND
            BANKDEFFXXX,Bank 2,Hauptstrasse 2,DE,GERMANY
            BANKFRPPXXX,Bank 3,Rue 3,FR,FRANCE
            """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        when(swiftCodeController.addSwiftCode(any(SwiftCode.class)))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Added")));

        csvParserService.parseAndStoreSwiftData(inputStream);

        verify(swiftCodeController, times(3)).addSwiftCode(any());
    }


    @Test
    void parseAndStoreSwiftData_shouldHandleMissingColumnsGracefully() {
        // Should not add anything if required column is missing
        String csvData = """
            SWIFT CODE,NAME,ADDRESS,COUNTRY ISO2 CODE
            BANKPLPWXXX,Bank Polska,Main St 1,PL
            """;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        csvParserService.parseAndStoreSwiftData(inputStream);

        verifyNoInteractions(swiftCodeController);
    }










}