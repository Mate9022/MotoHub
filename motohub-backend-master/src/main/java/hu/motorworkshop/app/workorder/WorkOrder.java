package hu.motorworkshop.app.workorder;

import hu.motorworkshop.app.motorcycle.Motorcycle;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "work_orders",
        indexes = {
                @Index(
                        name = "idx_work_order_motorcycle",
                        columnList = "motorcycle_id"
                ),
                @Index(
                        name = "idx_work_order_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_work_order_received_at",
                        columnList = "received_at"
                )
        }
)
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "work_order_number",
            nullable = false,
            unique = true,
            length = 32
    )
    private String workOrderNumber;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "motorcycle_id",
            nullable = false
    )
    private Motorcycle motorcycle;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private WorkOrderStatus status;


    @Column(
            name = "odometer_km",
            nullable = false
    )
    private Integer odometerKm;

    @Column(
            name = "handed_over_odometer_km"
    )
    private Integer handedOverOdometerKm;


    @Column(columnDefinition = "text")
    private String complaint;


    @Column(columnDefinition = "text")
    private String findings;


    @Column(columnDefinition = "text")
    private String recommendations;


    @Column(
            name = "received_at",
            nullable = false
    )
    private Instant receivedAt;


    @Column(name = "closed_at")
    private Instant closedAt;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Column(
            name = "ready_at"
    )
    private Instant readyAt;


    @Column(
            name = "handed_over_at"
    )
    private Instant handedOverAt;


    @OneToMany(
            mappedBy = "workOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<LaborItem> laborItems =
            new ArrayList<>();


    @OneToMany(
            mappedBy = "workOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PartItem> partItems =
            new ArrayList<>();


    @PrePersist
    void prePersist() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (receivedAt == null) {
            receivedAt = now;
        }

        if (status == null) {
            status = WorkOrderStatus.OPEN;
        }
    }


    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }


    public void addLaborItem(
            LaborItem item
    ) {

        laborItems.add(item);

        item.setWorkOrder(this);
    }


    public void removeLaborItem(
            LaborItem item
    ) {

        laborItems.remove(item);

        item.setWorkOrder(null);
    }


    public void addPartItem(
            PartItem item
    ) {

        partItems.add(item);

        item.setWorkOrder(this);
    }


    public void removePartItem(
            PartItem item
    ) {

        partItems.remove(item);

        item.setWorkOrder(null);
    }


    public UUID getId() {
        return id;
    }

    public Motorcycle getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(
            Motorcycle motorcycle
    ) {
        this.motorcycle = motorcycle;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(
            WorkOrderStatus status
    ) {
        this.status = status;
    }

    public Integer getOdometerKm() {
        return odometerKm;
    }

    public void setOdometerKm(
            Integer odometerKm
    ) {
        this.odometerKm = odometerKm;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(
            String complaint
    ) {
        this.complaint = complaint;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(
            String findings
    ) {
        this.findings = findings;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(
            String recommendations
    ) {
        this.recommendations = recommendations;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(
            Instant receivedAt
    ) {
        this.receivedAt = receivedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(
            Instant closedAt
    ) {
        this.closedAt = closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<LaborItem> getLaborItems() {
        return laborItems;
    }

    public List<PartItem> getPartItems() {
        return partItems;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(
            String workOrderNumber
    ) {
        this.workOrderNumber = workOrderNumber;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(
            Instant readyAt
    ) {
        this.readyAt = readyAt;
    }


    public Instant getHandedOverAt() {
        return handedOverAt;
    }

    public void setHandedOverAt(
            Instant handedOverAt
    ) {
        this.handedOverAt = handedOverAt;
    }

    public Integer getHandedOverOdometerKm() {
        return handedOverOdometerKm;
    }


    public void setHandedOverOdometerKm(
            Integer handedOverOdometerKm
    ) {
        this.handedOverOdometerKm =
                handedOverOdometerKm;
    }
}