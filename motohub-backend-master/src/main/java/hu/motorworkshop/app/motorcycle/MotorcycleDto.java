package hu.motorworkshop.app.motorcycle;

import java.time.Instant;
import java.util.UUID;

public record MotorcycleDto(
        UUID id,
        UUID customerId,
        String customerName,
        String brand,
        String model,
        Integer modelYear,
        String licensePlate,
        String vin,
        Instant createdAt,
        Instant updatedAt
) {
}