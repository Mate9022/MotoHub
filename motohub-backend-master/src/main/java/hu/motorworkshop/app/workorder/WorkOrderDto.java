package hu.motorworkshop.app.workorder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkOrderDto(

        UUID id,

        String workOrderNumber,

        WorkOrderStatus status,

        Integer odometerKm,

        Integer handedOverOdometerKm,

        String complaint,

        String findings,

        String recommendations,

        Instant receivedAt,

        Instant readyAt,

        Instant handedOverAt,

        Instant closedAt,

        Instant createdAt,

        Instant updatedAt,

        UUID motorcycleId,

        String motorcycle,

        String licensePlate,

        UUID customerId,

        String customerName,

        List<LaborItemDto> laborItems,

        List<PartItemDto> partItems,

        BigDecimal laborTotal,

        BigDecimal partsTotal,

        BigDecimal grandTotal

) {
}