package com.example.InternSwiftCodesProject.controllers;

import com.example.InternSwiftCodesProject.SWIFTCodeRepo;
import com.example.InternSwiftCodesProject.SwiftCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SWIFTCodeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SWIFTCodeRepo swiftCodeRepo;

    @BeforeEach
    void setup() {
        swiftCodeRepo.deleteAll(); // wyczyść bazę
        swiftCodeRepo.save(new SwiftCode("BANKPLPW", "Bank Polska", "Street 1", "PL", "POLAND", false));
    }

    @Nested
    class GetSwiftCode {
        @Test
        void shouldReturnSwiftCodeDetails_whenCodeExists() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/BANKPLPW"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.swiftCode").value("BANKPLPW"))
                    .andExpect(jsonPath("$.bankName").value("Bank Polska"));
        }

        @Test
        void shouldReturnNotFound_whenCodeDoesNotExist() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/FAKECODE1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("SWIFT code not found"));
        }

        @Test
        void shouldReturnBadRequest_whenCodeTooShort() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/PL"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid SWIFT code format"));
        }
    }
    @Nested
    class GetAllSwiftCodesWithISO2{

        @Test
        void shouldReturnSwiftCodesForValidCountry() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/country/PL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.countryISO2").value("PL"))
                    .andExpect(jsonPath("$.countryName").exists())
                    .andExpect(jsonPath("$.swiftCodes").isArray());
        }

        @Test
        void shouldReturnBadRequestPost_whenInvalidCountryCode() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/country/XYZ"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid country code"))
                    .andExpect(jsonPath("$.message").value("Country ISO2 code 'XYZ' is not valid."));
        }


        @Test
        void shouldReturnNotFound_whenNoSwiftCodesForCountry() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/country/AL")) // Albania np. jeśli pusto
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnSwiftCodes_whenCountryCodeIsLowercase() throws Exception {
            mockMvc.perform(get("/v1/swift-codes/country/pl"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.countryISO2").value("PL"));
        }
}
    @Nested
    class AddSwiftCode {

        @Test
        void shouldAddHeadquarterSuccessfully() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00XXX",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Warsaw HQ",
                        "bankName": "Test Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Headquarter added. Linked orphan branches if found."));
        }

        @Test
        void shouldAddBranchSuccessfully() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00AAA",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Branch in Krakow",
                        "bankName": "Test Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Branch added. Linked to HQ if exists."));
        }

        @Test
        void shouldReturnBadRequest_whenSwiftCodeTooShort() throws Exception {
            String json = """
                    {
                        "swiftCode": "PL00",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Short Swift",
                        "bankName": "Test Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid SWIFT code format"));
        }

        @Test
        void shouldReturnBadRequest_whenMissingFields() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00XXX",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Warsaw HQ"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Missing required fields"));
        }


        @Test
        void shouldReturnBadRequest_whenInvalidCountryCode() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTXX00XXX",
                        "countryISO2": "XX",
                        "countryName": "FAKELAND",
                        "address": "Nowhere",
                        "bankName": "Fake Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid country code"));
        }


        @Test
        void shouldReturnBadRequest_whenCountryNameMismatch() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00XXX",
                        "countryISO2": "PL",
                        "countryName": "GERMANY",
                        "address": "Warsaw HQ",
                        "bankName": "Mismatch Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Country name mismatch"));
        }


        @Test
        void shouldReturnConflict_whenSwiftCodeExists() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00XXX",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Warsaw HQ",
                        "bankName": "Test Bank"
                    }
                    """;

            // First insert
            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            // Try insert again
            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Duplicate SWIFT code"));
        }

        @Test
        void shouldReturnBadRequest_whenAddressTooShort() throws Exception {
            String json = """
                    {
                        "swiftCode": "TESTPL00AAA",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "AB",
                        "bankName": "Short Address Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Address must be between 3 and 500 characters"));
        }


        @Test
        void shouldReturnBadRequest_whenSwiftCodeHasInvalidCharacters() throws Exception {
            String json = """
                    {
                        "swiftCode": "TEST@!@#",
                        "countryISO2": "PL",
                        "countryName": "POLAND",
                        "address": "Valid Address",
                        "bankName": "Invalid Char Bank"
                    }
                    """;

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid characters in SWIFT code"));
        }
    }

    @Nested
    class DeleteSwiftCodeTests {

        @Test
        void shouldDeleteBranchSuccessfully() throws Exception {
            // Najpierw dodajemy branch (bez HQ)
            String swiftCode = "BRANCH123";
            String json = """
        {
            "swiftCode": "%s",
            "countryISO2": "PL",
            "countryName": "POLAND",
            "address": "Branch Street",
            "bankName": "Branch Bank"
        }
        """.formatted(swiftCode);

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            // Teraz go usuwamy
            mockMvc.perform(delete("/v1/swift-codes/" + swiftCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Branch deleted successfully."))
                    .andExpect(jsonPath("$.swiftCode").value(swiftCode));
        }

        @Test
        void shouldDeleteHeadquarterAndOrphanBranches() throws Exception {
            // Dodajemy HQ
            String hq = "HQ000000XXX";
            String hqJson = """
        {
            "swiftCode": "%s",
            "countryISO2": "PL",
            "countryName": "POLAND",
            "address": "HQ Address",
            "bankName": "HQ Bank"
        }
        """.formatted(hq);

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(hqJson))
                    .andExpect(status().isOk());

            // Dodajemy branch
            String branch = "HQ000000001";
            String branchJson = """
        {
            "swiftCode": "%s",
            "countryISO2": "PL",
            "countryName": "POLAND",
            "address": "Branch HQ",
            "bankName": "Branch Bank"
        }
        """.formatted(branch);

            mockMvc.perform(post("/v1/swift-codes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(branchJson))
                    .andExpect(status().isOk());

            // Usuwamy HQ
            mockMvc.perform(delete("/v1/swift-codes/" + hq))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Headquarter deleted")))
                    .andExpect(jsonPath("$.swiftCode").value(hq));
        }

        @Test
        void shouldReturnNotFound_whenSwiftCodeDoesNotExist() throws Exception {
            mockMvc.perform(delete("/v1/swift-codes/NOTEXIST123"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("SWIFT code not found"));
        }
    }




















}