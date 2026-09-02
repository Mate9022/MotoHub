package hu.motorworkshop.app.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(
            CustomerService service
    ) {
        this.service = service;
    }


    @GetMapping
    public List<CustomerDto> findAll(
            @RequestParam(
                    defaultValue = ""
            ) String search
    ) {
        return service.findAll(search);
    }


    @GetMapping("/{id}")
    public CustomerDto findById(
            @PathVariable UUID id
    ) {
        return service.findById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(
            @Valid
            @RequestBody
            CreateCustomerRequest request
    ) {
        return service.create(request);
    }


    @PutMapping("/{id}")
    public CustomerDto update(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            UpdateCustomerRequest request
    ) {
        return service.update(
                id,
                request
        );
    }
}