package hu.motorworkshop.app.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("""
        select c
        from Customer c
        where
            :search = ''
            or lower(c.name) like lower(concat('%', :search, '%'))
            or lower(coalesce(c.email, '')) like lower(concat('%', :search, '%'))
            or lower(coalesce(c.phone, '')) like lower(concat('%', :search, '%'))
        order by c.name asc
    """)
    List<Customer> search(
            @Param("search") String search
    );
}