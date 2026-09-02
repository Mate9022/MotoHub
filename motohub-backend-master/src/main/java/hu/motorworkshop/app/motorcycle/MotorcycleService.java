package hu.motorworkshop.app.motorcycle;

import hu.motorworkshop.app.common.NotFoundException;
import hu.motorworkshop.app.customer.Customer;
import hu.motorworkshop.app.customer.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MotorcycleService {

    private final MotorcycleRepository repository;
    private final CustomerService customerService;

    public MotorcycleService(
            MotorcycleRepository repository,
            CustomerService customerService
    ) {
        this.repository = repository;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public List<MotorcycleDto> findAll(String search) {

        String normalized =
                search == null ? "" : search.trim();

        return repository.search(normalized)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MotorcycleDto> findByCustomer(
            UUID customerId
    ) {
        customerService.getEntity(customerId);

        return repository
                .findByCustomerIdOrderByBrandAscModelAsc(customerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MotorcycleDto findById(UUID id) {
        return toDto(getEntity(id));
    }

    public MotorcycleDto create(
            CreateMotorcycleRequest request
    ) {

        Customer customer =
                customerService.getEntity(request.customerId());

        Motorcycle motorcycle = new Motorcycle();

        motorcycle.setCustomer(customer);
        motorcycle.setBrand(request.brand().trim());
        motorcycle.setModel(request.model().trim());
        motorcycle.setModelYear(request.modelYear());
        motorcycle.setLicensePlate(
                normalizeUpper(request.licensePlate())
        );
        motorcycle.setVin(
                normalizeUpper(request.vin())
        );

        return toDto(repository.save(motorcycle));
    }

    public MotorcycleDto update(
            UUID id,
            UpdateMotorcycleRequest request
    ) {

        Motorcycle motorcycle = getEntity(id);

        motorcycle.setBrand(request.brand().trim());
        motorcycle.setModel(request.model().trim());
        motorcycle.setModelYear(request.modelYear());
        motorcycle.setLicensePlate(
                normalizeUpper(request.licensePlate())
        );
        motorcycle.setVin(
                normalizeUpper(request.vin())
        );

        return toDto(motorcycle);
    }

    public Motorcycle getEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Motor nem található: " + id
                        )
                );
    }

    private MotorcycleDto toDto(
            Motorcycle motorcycle
    ) {
        return new MotorcycleDto(
                motorcycle.getId(),
                motorcycle.getCustomer().getId(),
                motorcycle.getCustomer().getName(),
                motorcycle.getBrand(),
                motorcycle.getModel(),
                motorcycle.getModelYear(),
                motorcycle.getLicensePlate(),
                motorcycle.getVin(),
                motorcycle.getCreatedAt(),
                motorcycle.getUpdatedAt()
        );
    }

    private String normalizeUpper(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized.toUpperCase();
    }
}