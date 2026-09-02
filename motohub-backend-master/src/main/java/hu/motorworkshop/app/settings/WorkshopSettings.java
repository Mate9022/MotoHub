package hu.motorworkshop.app.settings;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "workshop_settings")
public class WorkshopSettings {

    public static final Long SETTINGS_ID = 1L;

    @Id
    private Long id;

    @Column(
            name = "workshop_name",
            nullable = false,
            length = 200
    )
    private String workshopName;

    @Column(length = 300)
    private String address;

    @Column(length = 60)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(
            name = "tax_number",
            length = 60
    )
    private String taxNumber;

    @Column(
            name = "default_hourly_rate",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal defaultHourlyRate;

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


    protected WorkshopSettings() {
    }


    public WorkshopSettings(
            String workshopName,
            BigDecimal defaultHourlyRate
    ) {

        this.id = SETTINGS_ID;

        this.workshopName =
                workshopName;

        this.defaultHourlyRate =
                defaultHourlyRate;
    }


    @PrePersist
    void prePersist() {

        Instant now =
                Instant.now();

        createdAt = now;

        updatedAt = now;
    }


    @PreUpdate
    void preUpdate() {

        updatedAt =
                Instant.now();
    }


    public Long getId() {
        return id;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(
            String workshopName
    ) {
        this.workshopName = workshopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(
            String address
    ) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(
            String phone
    ) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(
            String taxNumber
    ) {
        this.taxNumber = taxNumber;
    }

    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(
            BigDecimal defaultHourlyRate
    ) {
        this.defaultHourlyRate =
                defaultHourlyRate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}