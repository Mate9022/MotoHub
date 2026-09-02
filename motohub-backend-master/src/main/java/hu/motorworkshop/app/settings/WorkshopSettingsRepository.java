package hu.motorworkshop.app.settings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopSettingsRepository
        extends JpaRepository<WorkshopSettings, Long> {
}