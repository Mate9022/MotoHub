package hu.motorworkshop.app.workorder;

import java.math.BigDecimal;
import java.util.UUID;

public record PartItemDto(

        UUID id,

        String description,

        String sku,

        BigDecimal quantity,

        BigDecimal unitPrice,

        BigDecimal total

) {
}