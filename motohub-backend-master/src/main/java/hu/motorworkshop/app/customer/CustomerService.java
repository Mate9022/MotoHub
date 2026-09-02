package hu.motorworkshop.app.customer;

import hu.motorworkshop.app.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(
            CustomerRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public List<CustomerDto> findAll(
            String search
    ) {

        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        return repository
                .search(normalizedSearch)
                .stream()
                .map(this::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public CustomerDto findById(
            UUID id
    ) {
        return toDto(
                getEntity(id)
        );
    }


    public CustomerDto create(
            CreateCustomerRequest request
    ) {

        Customer customer = new Customer();

        customer.setName(
                normalizeRequired(request.name())
        );

        customer.setPhone(
                normalizeOptional(request.phone())
        );

        customer.setEmail(
                normalizeOptional(request.email())
        );

        return toDto(
                repository.save(customer)
        );
    }


    public CustomerDto update(
            UUID id,
            UpdateCustomerRequest request
    ) {

        Customer customer =
                getEntity(id);

        customer.setName(
                normalizeRequired(request.name())
        );

        customer.setPhone(
                normalizeOptional(request.phone())
        );

        customer.setEmail(
                normalizeOptional(request.email())
        );

        return toDto(customer);
    }


    public Customer getEntity(
            UUID id
    ) {
        return repository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Ügyfél nem található: " + id
                        )
                );
    }


    private CustomerDto toDto(
            Customer customer
    ) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }


    private String normalizeRequired(
            String value
    ) {
        return value.trim();
    }


    private String normalizeOptional(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}