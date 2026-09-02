package hu.motorworkshop.app.workorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePartItemRequest(

        @NotBlank
        @Size(max = 240)
        String description,

        @Size(max = 120)
        String sku,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal quantity,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal unitPrice

) {
}