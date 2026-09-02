package hu.motorworkshop.app.motorcycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMotorcycleRequest(

        @NotBlank
        @Size(max = 100)
        String brand,

        @NotBlank
        @Size(max = 120)
        String model,

        Integer modelYear,

        @Size(max = 30)
        String licensePlate,

        @Size(max = 80)
        String vin

) {
}