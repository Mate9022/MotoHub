package hu.motorworkshop.app.workorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "work_order_number_counters"
)
public class WorkOrderNumberCounter {

    @Id
    @Column(name = "counter_year")
    private Integer year;

    @Column(
            name = "last_number",
            nullable = false
    )
    private Long lastNumber;


    protected WorkOrderNumberCounter() {
    }


    public Integer getYear() {
        return year;
    }

    public Long getLastNumber() {
        return lastNumber;
    }
}