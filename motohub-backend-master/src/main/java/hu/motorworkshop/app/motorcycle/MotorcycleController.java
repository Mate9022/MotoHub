package hu.motorworkshop.app.motorcycle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/motorcycles")
public class MotorcycleController {

    private final MotorcycleService service;

    public MotorcycleController(
            MotorcycleService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<MotorcycleDto> findAll(
            @RequestParam(defaultValue = "") String search
    ) {
        return service.findAll(search);
    }

    @GetMapping("/{id}")
    public MotorcycleDto findById(
            @PathVariable UUID id
    ) {
        return service.findById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<MotorcycleDto> findByCustomer(
            @PathVariable UUID customerId
    ) {
        return service.findByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MotorcycleDto create(
            @Valid @RequestBody
            CreateMotorcycleRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MotorcycleDto update(
            @PathVariable UUID id,
            @Valid @RequestBody
            UpdateMotorcycleRequest request
    ) {
        return service.update(id, request);
    }
}