package hu.motorworkshop.app.workorder;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "part_items",
        indexes = {
                @Index(
                        name = "idx_part_item_work_order",
                        columnList = "work_order_id"
                )
        }
)
public class PartItem {

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


    @Column(length = 120)
    private String sku;


    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal quantity;


    @Column(
            name = "unit_price",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal unitPrice;


    public BigDecimal calculateTotal() {

        return quantity.multiply(
                unitPrice
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

    public String getSku() {
        return sku;
    }

    public void setSku(
            String sku
    ) {
        this.sku = sku;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(
            BigDecimal quantity
    ) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(
            BigDecimal unitPrice
    ) {
        this.unitPrice = unitPrice;
    }
}