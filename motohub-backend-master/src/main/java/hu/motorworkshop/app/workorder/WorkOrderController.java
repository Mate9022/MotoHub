package hu.motorworkshop.app.workorder;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService service;


    public WorkOrderController(
            WorkOrderService service
    ) {

        this.service = service;
    }


    @GetMapping
    public List<WorkOrderDto> findAll(

            @RequestParam(
                    defaultValue = ""
            )
            String search

    ) {

        return service.findAll(
                search
        );
    }


    @GetMapping("/{id}")
    public WorkOrderDto findById(

            @PathVariable
            UUID id

    ) {

        return service.findById(
                id
        );
    }


    @GetMapping(
            "/motorcycle/{motorcycleId}"
    )
    public List<WorkOrderDto> findByMotorcycle(

            @PathVariable
            UUID motorcycleId

    ) {

        return service.findByMotorcycle(
                motorcycleId
        );
    }


    @PostMapping
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public WorkOrderDto create(

            @Valid
            @RequestBody
            CreateWorkOrderRequest request

    ) {

        return service.create(
                request
        );
    }


    @PutMapping("/{id}")
    public WorkOrderDto update(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateWorkOrderRequest request

    ) {

        return service.update(
                id,
                request
        );
    }


    // --------------------------
    // LABOR
    // --------------------------

    @PostMapping(
            "/{id}/labor-items"
    )
    public WorkOrderDto addLaborItem(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            CreateLaborItemRequest request

    ) {

        return service.addLaborItem(
                id,
                request
        );
    }


    @PutMapping(
            "/{id}/labor-items/{itemId}"
    )
    public WorkOrderDto updateLaborItem(

            @PathVariable
            UUID id,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdateLaborItemRequest request

    ) {

        return service.updateLaborItem(
                id,
                itemId,
                request
        );
    }


    @DeleteMapping(
            "/{id}/labor-items/{itemId}"
    )
    public WorkOrderDto deleteLaborItem(

            @PathVariable
            UUID id,

            @PathVariable
            UUID itemId

    ) {

        return service.deleteLaborItem(
                id,
                itemId
        );
    }


    // --------------------------
    // PARTS
    // --------------------------

    @PostMapping(
            "/{id}/part-items"
    )
    public WorkOrderDto addPartItem(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            CreatePartItemRequest request

    ) {

        return service.addPartItem(
                id,
                request
        );
    }


    @PutMapping(
            "/{id}/part-items/{itemId}"
    )
    public WorkOrderDto updatePartItem(

            @PathVariable
            UUID id,

            @PathVariable
            UUID itemId,

            @Valid
            @RequestBody
            UpdatePartItemRequest request

    ) {

        return service.updatePartItem(
                id,
                itemId,
                request
        );
    }


    @DeleteMapping(
            "/{id}/part-items/{itemId}"
    )
    public WorkOrderDto deletePartItem(

            @PathVariable
            UUID id,

            @PathVariable
            UUID itemId

    ) {

        return service.deletePartItem(
                id,
                itemId
        );
    }


    // --------------------------
    // CLOSE / REOPEN
    // --------------------------

    @PostMapping("/{id}/close")
    public WorkOrderDto close(

            @PathVariable
            UUID id

    ) {

        return service.close(
                id
        );
    }


    @PostMapping("/{id}/reopen")
    public WorkOrderDto reopen(

            @PathVariable
            UUID id

    ) {

        return service.reopen(
                id
        );
    }

    @PostMapping("/{id}/ready")
    public WorkOrderDto markReady(
            @PathVariable UUID id
    ) {

        return service.markReady(
                id
        );
    }


    @PostMapping("/{id}/handover")
    public WorkOrderDto handOver(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            HandOverWorkOrderRequest request

    ) {

        return service.handOver(
                id,
                request
        );
    }
}