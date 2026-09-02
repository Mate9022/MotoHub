package hu.motorworkshop.app.motorcycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MotorcycleRepository
        extends JpaRepository<Motorcycle, UUID> {

    List<Motorcycle> findByCustomerIdOrderByBrandAscModelAsc(
            UUID customerId
    );

    @Query("""
        select m
        from Motorcycle m
        where
            :search = ''
            or lower(m.brand) like lower(concat('%', :search, '%'))
            or lower(m.model) like lower(concat('%', :search, '%'))
            or lower(coalesce(m.licensePlate, '')) like lower(concat('%', :search, '%'))
            or lower(coalesce(m.vin, '')) like lower(concat('%', :search, '%'))
        order by m.brand, m.model
    """)
    List<Motorcycle> search(
            @Param("search") String search
    );
}