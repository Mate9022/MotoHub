package hu.motorworkshop.app.photo;

import hu.motorworkshop.app.workorder.WorkOrder;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "work_order_photos"
)
public class WorkOrderPhoto {

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
            name = "original_file_name",
            nullable = false,
            length = 500
    )
    private String originalFileName;


    @Column(
            name = "stored_file_name",
            nullable = false,
            unique = true,
            length = 200
    )
    private String storedFileName;


    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;


    @Column(
            name = "file_size",
            nullable = false
    )
    private Long fileSize;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


    protected WorkOrderPhoto() {
    }


    @PrePersist
    void prePersist() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(
            String originalFileName
    ) {
        this.originalFileName =
                originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(
            String storedFileName
    ) {
        this.storedFileName =
                storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(
            String contentType
    ) {
        this.contentType =
                contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(
            Long fileSize
    ) {
        this.fileSize =
                fileSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}