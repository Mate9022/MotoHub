package hu.motorworkshop.app.workorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWorkOrderRequest(

        @NotNull
        WorkOrderStatus status,

        @NotNull
        @Min(0)
        Integer odometerKm,

        @Size(max = 10000)
        String complaint,

        @Size(max = 10000)
        String findings,

        @Size(max = 10000)
        String recommendations

) {
}