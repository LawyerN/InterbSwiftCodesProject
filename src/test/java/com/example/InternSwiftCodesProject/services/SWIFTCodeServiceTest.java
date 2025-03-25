package com.example.InternSwiftCodesProject.services;

import com.example.InternSwiftCodesProject.SWIFTCodeRepo;
import com.example.InternSwiftCodesProject.SwiftCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SWIFTCodeServiceTest {
    @Mock
    private SWIFTCodeRepo swiftCodeRepo; // fake repo

    @InjectMocks
    private SWIFTCodeService service; // our tested class

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // mock initialization
    }

    @Test
    void isValidCountryCode() {
        assertTrue(SWIFTCodeService.isValidCountryCode("PL"));
    }

    @Test
    void isValidCountryCode_shouldReturnFalseForInvalidCode() {
        assertFalse(SWIFTCodeService.isValidCountryCode("ZZ"));
    }

    @Test
    void isValidCountryCode_shouldReturnFalseForLowercaseCode() {
        assertFalse(SWIFTCodeService.isValidCountryCode("pl"));
    }

    @Test
    void isValidCountryCode_shouldReturnFalseForEmptyString() {
        assertFalse(SWIFTCodeService.isValidCountryCode(""));
    }

    @Test
    void getCorrectCountryName_shouldReturnNameForValidCode() {
        assertEquals("POLAND", SWIFTCodeService.getCorrectCountryName("PL"));
    }

    @Test
    void getCorrectCountryName_shouldReturnUnknownForInvalidCode() {
        assertEquals("UNKNOWN", SWIFTCodeService.getCorrectCountryName("XX"));
    }

    @Test
    void getCorrectCountryName_shouldReturnUnknownForLowercaseCode() {
        assertEquals("UNKNOWN", SWIFTCodeService.getCorrectCountryName("pl"));
    }

    @Test
    void getCorrectCountryName_shouldReturnUnknownForEmptyCode() {
        assertEquals("UNKNOWN", SWIFTCodeService.getCorrectCountryName(""));
    }

    @Test
    void getSwiftCodesByCountry_shouldReturnSwiftCodesList() {
        // ARRANGE – test dataset
        SwiftCode swift = new SwiftCode();
        swift.setSwiftCode("BANKPLPW");
        swift.setCountryISO2("PL");

        List<SwiftCode> mockResult = List.of(swift);

        // Mockito – If someonecall repo with "Pl" it will return our list
        when(swiftCodeRepo.findByCountryISO2IgnoreCase("PL")).thenReturn(mockResult);

        // ACT – we call our method
        List<SwiftCode> result = service.getSwiftCodesByCountry("PL");

        // ASSERT – check if everything is ok
        assertEquals(1, result.size());
        assertEquals("BANKPLPW", result.get(0).getSwiftCode());


    }

    @Test
    void getSwiftCodesByCountry_shouldReturnEmptyListIfNothingFound() {
        // Mockito  returns empty list if called with "XX"
        when(swiftCodeRepo.findByCountryISO2IgnoreCase("XX")).thenReturn(List.of());

        List<SwiftCode> result = service.getSwiftCodesByCountry("XX");

        assertTrue(result.isEmpty());
    }

    @Test
    void findBranchesByPrefix_shouldReturnListOfBranches() {
        // given
        SwiftCode branch = new SwiftCode();
        branch.setSwiftCode("BANKPLPWXXX");
        branch.setHeadquarterFlag(false);

        when(swiftCodeRepo.findBySwiftCodeStartingWithAndHeadquarterFlagFalse("BANK"))
                .thenReturn(List.of(branch));

        // when
        List<SwiftCode> result = service.findBranchesByPrefix("BANK");

        // then
        assertEquals(1, result.size());
        assertEquals("BANKPLPWXXX", result.get(0).getSwiftCode());
        assertFalse(result.get(0).isHeadquarterFlag());

    }

    @Test
    void findBranchesByPrefix_shouldReturnEmptyListIfNoneFound() {
        when(swiftCodeRepo.findBySwiftCodeStartingWithAndHeadquarterFlagFalse("XXXX"))
                .thenReturn(List.of());

        List<SwiftCode> result = service.findBranchesByPrefix("XXXX");

        assertTrue(result.isEmpty());
    }

    @Test
    void getSwiftCodeDetails_shouldReturnSwiftCodeIfExists() {
        // given
        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode("BANKPLPW");

        when(swiftCodeRepo.findById("BANKPLPW")).thenReturn(Optional.of(swiftCode));

        // when
        Optional<SwiftCode> result = service.getSwiftCodeDetails("bankplpw"); // lowercase input

        // then
        assertTrue(result.isPresent());
        assertEquals("BANKPLPW", result.get().getSwiftCode());

        verify(swiftCodeRepo, times(1)).findById("BANKPLPW");
    }

    @Test
    void getSwiftCodeDetails_shouldReturnEmptyIfNotFound() {
        when(swiftCodeRepo.findById("XXXXXXX")).thenReturn(Optional.empty());

        Optional<SwiftCode> result = service.getSwiftCodeDetails("xxxxxxx");

        assertTrue(result.isEmpty());

        verify(swiftCodeRepo, times(1)).findById("XXXXXXX");
    }

    @Test
    void getBranchesForHeadquarter_shouldReturnBranchList() {
        // given
        SwiftCode branch1 = new SwiftCode();
        branch1.setSwiftCode("BANKPLPW123");
        SwiftCode branch2 = new SwiftCode();
        branch2.setSwiftCode("BANKPLPW456");

        when(swiftCodeRepo.findByHeadquarter_SwiftCode("BANKPLPWXXX"))
                .thenReturn(List.of(branch1, branch2));

        // when
        List<SwiftCode> result = service.getBranchesForHeadquarter("BANKPLPWXXX");

        // then
        assertEquals(2, result.size());
        assertEquals("BANKPLPW123", result.get(0).getSwiftCode());
        assertEquals("BANKPLPW456", result.get(1).getSwiftCode());

    }

    @Test
    void getBranchesForHeadquarter_shouldReturnEmptyListIfNoneFound() {
        when(swiftCodeRepo.findByHeadquarter_SwiftCode("NONEXISTENT"))
                .thenReturn(List.of());

        List<SwiftCode> result = service.getBranchesForHeadquarter("NONEXISTENT");

        assertTrue(result.isEmpty());
    }


    @Test
    void saveSwiftCode_shouldSaveBranchSuccessfully() {
        SwiftCode branch = new SwiftCode();
        branch.setSwiftCode("TESTPLPW");
        branch.setHeadquarterFlag(false);

        when(swiftCodeRepo.existsBySwiftCode("TESTPLPW")).thenReturn(false);

        SwiftCode saved = service.saveSwiftCode(branch);

        assertEquals("TESTPLPW", saved.getSwiftCode());
        verify(swiftCodeRepo).save(branch);
    }

    @Test
    void saveSwiftCode_shouldSaveHeadquarterWithoutOrphanBranches() {
        SwiftCode hq = new SwiftCode();
        hq.setSwiftCode("BANKPLPWXXX");
        hq.setHeadquarterFlag(true);

        when(swiftCodeRepo.existsBySwiftCode("BANKPLPWXXX")).thenReturn(false);
        when(swiftCodeRepo.findByHeadquarterIsNullAndSwiftCodeStartingWith("BANKPLPW"))
                .thenReturn(List.of()); // No orphaned branches

        SwiftCode saved = service.saveSwiftCode(hq);

        assertEquals("BANKPLPWXXX", saved.getSwiftCode());
        verify(swiftCodeRepo).save(hq);
    }

    @Test
    void saveSwiftCode_shouldReassignOrphanBranchesToNewHeadquarter() {
        SwiftCode hq = new SwiftCode();
        hq.setSwiftCode("HQPLPLPWXXX");
        hq.setHeadquarterFlag(true);

        SwiftCode orphan1 = new SwiftCode();
        orphan1.setSwiftCode("HQPLPLPW1");
        orphan1.setHeadquarter(null);

        SwiftCode orphan2 = new SwiftCode();
        orphan2.setSwiftCode("HQPLPLPW2");
        orphan2.setHeadquarter(null);

        when(swiftCodeRepo.existsBySwiftCode("HQPLPLPWXXX")).thenReturn(false);
        when(swiftCodeRepo.findByHeadquarterIsNullAndSwiftCodeStartingWith("HQPLPLPW"))
                .thenReturn(List.of(orphan1, orphan2));

        SwiftCode saved = service.saveSwiftCode(hq);

        assertEquals("HQPLPLPWXXX", saved.getSwiftCode());

        verify(swiftCodeRepo).save(hq);
        verify(swiftCodeRepo).save(orphan1);
        verify(swiftCodeRepo).save(orphan2);
        assertEquals(hq, orphan1.getHeadquarter());
        assertEquals(hq, orphan2.getHeadquarter());
    }



    @Test
    void saveSwiftCode_shouldThrowExceptionWhenSwiftCodeExists() {
        //Should throw an exception when trying to save a swift code that already exists
        SwiftCode duplicate = new SwiftCode();
        duplicate.setSwiftCode("BANKPLPW");
        duplicate.setHeadquarterFlag(false);

        when(swiftCodeRepo.existsBySwiftCode("BANKPLPW")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            service.saveSwiftCode(duplicate);
        });

    }

    @Test
    void deleteSwiftCode_shouldReturnFalseIfCodeNotFound() {
        // Should return false when the SWIFT code does not exist

        when(swiftCodeRepo.findById("MISSING")).thenReturn(Optional.empty());

        boolean result = service.deleteSwiftCode("MISSING");

        assertFalse(result);
    }

    @Test
    void deleteSwiftCode_shouldDeleteBranchSuccessfully() {
        // Should delete branch when found and is not HQ

        SwiftCode branch = new SwiftCode();
        branch.setSwiftCode("BANKPLPW");
        branch.setHeadquarterFlag(false);

        when(swiftCodeRepo.findById("BANKPLPW")).thenReturn(Optional.of(branch));

        boolean result = service.deleteSwiftCode("BANKPLPW");

        assertTrue(result);
        verify(swiftCodeRepo).deleteById("BANKPLPW");
    }


    @Test
    void deleteSwiftCode_shouldUnlinkBranchesAndDeleteHQ() {
        // Should unlink all branches from HQ and delete HQ

        SwiftCode hq = new SwiftCode();
        hq.setSwiftCode("HQPLPWXXX");
        hq.setHeadquarterFlag(true);

        SwiftCode branch1 = new SwiftCode();
        branch1.setSwiftCode("HQPLPW1");
        branch1.setHeadquarter(hq);

        SwiftCode branch2 = new SwiftCode();
        branch2.setSwiftCode("HQPLPW2");
        branch2.setHeadquarter(hq);

        when(swiftCodeRepo.findById("HQPLPWXXX")).thenReturn(Optional.of(hq));
        when(swiftCodeRepo.findByHeadquarter(hq)).thenReturn(List.of(branch1, branch2));

        boolean result = service.deleteSwiftCode("HQPLPWXXX");

        assertTrue(result);
        // Check that headquarter references are removed

        assertNull(branch1.getHeadquarter());
        assertNull(branch2.getHeadquarter());
        // Verify that changes were saved and HQ deleted
        verify(swiftCodeRepo).save(branch1);
        verify(swiftCodeRepo).save(branch2);
        verify(swiftCodeRepo).deleteById("HQPLPWXXX");
    }


    @Test
    void deleteSwiftCode_shouldDeleteHQWithoutBranches() {
        // Should delete HQ when it has no linked branches

        SwiftCode hq = new SwiftCode();
        hq.setSwiftCode("HQPLPWXXX");
        hq.setHeadquarterFlag(true);

        when(swiftCodeRepo.findById("HQPLPWXXX")).thenReturn(Optional.of(hq));
        when(swiftCodeRepo.findByHeadquarter(hq)).thenReturn(List.of());

        boolean result = service.deleteSwiftCode("HQPLPWXXX");

        assertTrue(result);
        verify(swiftCodeRepo, never()).save(any());
        verify(swiftCodeRepo).deleteById("HQPLPWXXX");
    }


    @Test
    void existsBySwiftCode_shouldReturnTrueIfExists() {
        // Should return true if SWIFT code exists (case insensitive)

        when(swiftCodeRepo.existsBySwiftCode("BANKPLPW")).thenReturn(true);

        boolean result = service.existsBySwiftCode("bankplpw"); // lowercase input

        assertTrue(result);
        verify(swiftCodeRepo).existsBySwiftCode("BANKPLPW");
    }

    @Test
    void existsBySwiftCode_shouldReturnFalseIfNotExists() {
        // Should return false if SWIFT code does not exist
        when(swiftCodeRepo.existsBySwiftCode("UNKNOWN")).thenReturn(false);

        boolean result = service.existsBySwiftCode("unknown");

        assertFalse(result);
        verify(swiftCodeRepo).existsBySwiftCode("UNKNOWN");
    }







}