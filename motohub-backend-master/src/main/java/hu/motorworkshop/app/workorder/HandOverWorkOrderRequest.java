package hu.motorworkshop.app.workorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HandOverWorkOrderRequest(

        @NotNull
        @Min(0)
        Integer odometerKm

) {
}