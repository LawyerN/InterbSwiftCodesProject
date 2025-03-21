package com.example.atakiprojekt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({ "address", "bankName", "countryISO2", "countryName", "isHeadquarter", "swiftCode", "branches" })
public class SwiftCodeWithBranchesDTO {
    @JsonProperty("address")
    private String address;
    @JsonProperty("bankName")
    private String bankName;
    @JsonProperty("countryISO2")
    private String countryISO2;
    @JsonProperty("countryName")
    private String countryName;
    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;
    @JsonProperty("swiftCode")
    private String swiftCode;
    @JsonProperty("branches")
    private List<SWIFTCodeDTO> branches;
    public String getAddress() {
        return (address == null || address.trim().isEmpty()) ? "No address avaliable" : address;
    }
}
