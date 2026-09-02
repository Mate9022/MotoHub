package hu.motorworkshop.app.workorder;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "labor_items",
        indexes = {
                @Index(
                        name = "idx_labor_item_work_order",
                        columnList = "work_order_id"
                )
        }
)
public class LaborItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "work_order_id",
            nullable = false
    )
    private WorkOrder workOrder;


    @Column(
            nullable = false,
            length = 240
    )
    private String description;


    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal hours;


    @Column(
            name = "hourly_rate",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal hourlyRate;


    public BigDecimal calculateTotal() {

        return hours.multiply(
                hourlyRate
        );
    }


    public UUID getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(
            WorkOrder workOrder
    ) {
        this.workOrder = workOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(
            BigDecimal hours
    ) {
        this.hours = hours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(
            BigDecimal hourlyRate
    ) {
        this.hourlyRate = hourlyRate;
    }
}