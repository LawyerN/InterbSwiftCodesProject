package com.example.InternSwiftCodesProject;
import jakarta.persistence.*;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "swift_codes")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SwiftCode {
    @Id
    @Column(name = "swiftCode", unique = true, nullable = false)
    @NotNull
    private String swiftCode;

    private String bankName;
    private String address;
    private String countryISO2;
    private String countryName;
    private boolean  headquarterFlag;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name="headquarter_swift", referencedColumnName = "swiftCode")
    private SwiftCode headquarter;

    public SwiftCode(String swiftCode, String bankName, String address, String countryISO2, String countryName, boolean isHeadquarter) {
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.countryISO2 = countryISO2;
        this.countryName = countryName;
        this.headquarterFlag = isHeadquarter;
        this.headquarter = null;
    }
}
