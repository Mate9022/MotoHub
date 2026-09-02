package hu.motorworkshop.app.workorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository
        extends JpaRepository<WorkOrder, UUID> {


    @Query("""
        select w
        from WorkOrder w
        join fetch w.motorcycle m
        join fetch m.customer c
        where
            :search = ''
            or lower(m.brand)
                like lower(concat('%', :search, '%'))
            or lower(m.model)
                like lower(concat('%', :search, '%'))
            or lower(coalesce(m.licensePlate, ''))
                like lower(concat('%', :search, '%'))
            or lower(coalesce(m.vin, ''))
                like lower(concat('%', :search, '%'))
            or lower(c.name)
                like lower(concat('%', :search, '%'))
            or lower(w.workOrderNumber)
                like lower(concat('%', :search, '%'))
        order by w.receivedAt desc
    """)
    List<WorkOrder> search(
            @Param("search")
            String search
    );


    @Query("""
        select w
        from WorkOrder w
        join fetch w.motorcycle m
        join fetch m.customer c
        where w.id = :id
    """)
    Optional<WorkOrder> findDetailedById(
            @Param("id")
            UUID id
    );


    @Query("""
        select w
        from WorkOrder w
        join fetch w.motorcycle m
        join fetch m.customer c
        where m.id = :motorcycleId
        order by w.receivedAt desc
    """)
    List<WorkOrder> findByMotorcycleId(
            @Param("motorcycleId")
            UUID motorcycleId
    );
}