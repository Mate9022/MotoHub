package hu.motorworkshop.app.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class WorkshopSettingsService {

    private final WorkshopSettingsRepository repository;


    public WorkshopSettingsService(
            WorkshopSettingsRepository repository
    ) {

        this.repository =
                repository;
    }


    @Transactional(readOnly = true)
    public WorkshopSettingsDto get() {

        return repository
                .findById(
                        WorkshopSettings.SETTINGS_ID
                )
                .map(this::toDto)
                .orElseGet(
                        this::getDefaultDto
                );
    }


    public WorkshopSettingsDto update(
            UpdateWorkshopSettingsRequest request
    ) {

        WorkshopSettings settings =
                repository
                        .findById(
                                WorkshopSettings.SETTINGS_ID
                        )
                        .orElseGet(
                                this::createDefaultEntity
                        );


        settings.setWorkshopName(
                request
                        .workshopName()
                        .trim()
        );


        settings.setAddress(
                normalizeOptional(
                        request.address()
                )
        );


        settings.setPhone(
                normalizeOptional(
                        request.phone()
                )
        );


        settings.setEmail(
                normalizeOptional(
                        request.email()
                )
        );


        settings.setTaxNumber(
                normalizeOptional(
                        request.taxNumber()
                )
        );


        settings.setDefaultHourlyRate(
                request.defaultHourlyRate()
        );


        WorkshopSettings saved =
                repository.save(settings);


        return toDto(saved);
    }


    private WorkshopSettings createDefaultEntity() {

        return new WorkshopSettings(
                "Motor Műhely",
                BigDecimal.ZERO
        );
    }


    private WorkshopSettingsDto getDefaultDto() {

        return new WorkshopSettingsDto(
                "Motor Műhely",
                null,
                null,
                null,
                null,
                BigDecimal.ZERO,
                null,
                null
        );
    }


    private WorkshopSettingsDto toDto(
            WorkshopSettings settings
    ) {

        return new WorkshopSettingsDto(

                settings.getWorkshopName(),

                settings.getAddress(),

                settings.getPhone(),

                settings.getEmail(),

                settings.getTaxNumber(),

                settings.getDefaultHourlyRate(),

                settings.getCreatedAt(),

                settings.getUpdatedAt()
        );
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