package hu.motorworkshop.app.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String name,
        String phone,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
}