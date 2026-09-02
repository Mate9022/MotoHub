package hu.motorworkshop.app.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank
        @Size(max = 160)
        String name,

        @Size(max = 60)
        String phone,

        @Email
        @Size(max = 160)
        String email

) {
}