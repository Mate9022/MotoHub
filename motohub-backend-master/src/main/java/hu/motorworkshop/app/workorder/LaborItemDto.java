package hu.motorworkshop.app.workorder;

import java.math.BigDecimal;
import java.util.UUID;

public record LaborItemDto(

        UUID id,

        String description,

        BigDecimal hours,

        BigDecimal hourlyRate,

        BigDecimal total

) {
}