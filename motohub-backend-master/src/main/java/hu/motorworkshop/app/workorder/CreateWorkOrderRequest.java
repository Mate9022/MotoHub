package hu.motorworkshop.app.workorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkOrderRequest(

        @NotNull
        UUID motorcycleId,

        @NotNull
        @Min(0)
        Integer odometerKm,

        @Size(max = 10000)
        String complaint

) {
}