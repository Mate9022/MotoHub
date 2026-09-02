package hu.motorworkshop.app.workorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateLaborItemRequest(

        @NotBlank
        @Size(max = 240)
        String description,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal hours,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal hourlyRate

) {
}