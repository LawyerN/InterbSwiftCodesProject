package com.example.InternSwiftCodesProject.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"address", "bankName", "countryISO2", "isHeadquarter", "swiftCode"})
public class SWIFTCodeSimpleDTO {
    @JsonProperty("address")
    private String address;
    @JsonProperty("bankName")
    private String bankName;
    @JsonProperty("countryISO2")
    private String countryISO2;
    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;
    @JsonProperty("swiftCode")
    private String swiftCode;
}
