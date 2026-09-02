package hu.motorworkshop.app.settings;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkshopSettingsDto(

        String workshopName,

        String address,

        String phone,

        String email,

        String taxNumber,

        BigDecimal defaultHourlyRate,

        Instant createdAt,

        Instant updatedAt

) {
}