package hu.motorworkshop.app.settings;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class WorkshopSettingsController {

    private final WorkshopSettingsService service;


    public WorkshopSettingsController(
            WorkshopSettingsService service
    ) {

        this.service =
                service;
    }


    @GetMapping
    public WorkshopSettingsDto get() {

        return service.get();
    }


    @PutMapping
    public WorkshopSettingsDto update(

            @Valid
            @RequestBody
            UpdateWorkshopSettingsRequest request

    ) {

        return service.update(
                request
        );
    }
}