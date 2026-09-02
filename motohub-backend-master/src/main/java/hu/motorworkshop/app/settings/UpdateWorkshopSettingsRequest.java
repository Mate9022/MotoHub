package hu.motorworkshop.app.settings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateWorkshopSettingsRequest(

        @NotBlank
        @Size(max = 200)
        String workshopName,

        @Size(max = 300)
        String address,

        @Size(max = 60)
        String phone,

        @Email
        @Size(max = 160)
        String email,

        @Size(max = 60)
        String taxNumber,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal defaultHourlyRate

) {
}