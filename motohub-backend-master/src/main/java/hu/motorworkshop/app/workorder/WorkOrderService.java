package hu.motorworkshop.app.workorder;

import hu.motorworkshop.app.common.NotFoundException;
import hu.motorworkshop.app.motorcycle.Motorcycle;
import hu.motorworkshop.app.motorcycle.MotorcycleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository repository;

    private final MotorcycleService motorcycleService;

    private final WorkOrderNumberService workOrderNumberService;


    public WorkOrderService(
            WorkOrderRepository repository,
            MotorcycleService motorcycleService, WorkOrderNumberService workOrderNumberService
    ) {

        this.repository = repository;

        this.motorcycleService =
                motorcycleService;
        this.workOrderNumberService = workOrderNumberService;
    }


    // --------------------------------
    // LISTA
    // --------------------------------

    @Transactional(readOnly = true)
    public List<WorkOrderDto> findAll(
            String search
    ) {

        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();


        return repository
                .search(normalizedSearch)
                .stream()
                .map(this::toDto)
                .toList();
    }


    // --------------------------------
    // EGY MUNKALAP
    // --------------------------------

    @Transactional(readOnly = true)
    public WorkOrderDto findById(
            UUID id
    ) {

        return toDto(
                getEntity(id)
        );
    }


    // --------------------------------
    // MOTOR SZERVIZTÖRTÉNET
    // --------------------------------

    @Transactional(readOnly = true)
    public List<WorkOrderDto> findByMotorcycle(
            UUID motorcycleId
    ) {

        motorcycleService.getEntity(
                motorcycleId
        );


        return repository
                .findByMotorcycleId(
                        motorcycleId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }


    // --------------------------------
    // LÉTREHOZÁS
    // --------------------------------

    public WorkOrderDto create(
            CreateWorkOrderRequest request
    ) {

        Motorcycle motorcycle =
                motorcycleService.getEntity(
                        request.motorcycleId()
                );


        WorkOrder workOrder =
                new WorkOrder();


        workOrder.setWorkOrderNumber(
                workOrderNumberService.nextNumber()
        );


        workOrder.setMotorcycle(
                motorcycle
        );


        workOrder.setOdometerKm(
                request.odometerKm()
        );


        workOrder.setComplaint(
                normalizeOptional(
                        request.complaint()
                )
        );


        workOrder.setStatus(
                WorkOrderStatus.OPEN
        );


        return toDto(
                repository.save(
                        workOrder
                )
        );
    }


    // --------------------------------
    // MUNKALAP MÓDOSÍTÁSA
    // --------------------------------

    public WorkOrderDto update(
            UUID id,
            UpdateWorkOrderRequest request
    ) {

        WorkOrder workOrder =
                getEditableEntity(id);


        workOrder.setOdometerKm(
                request.odometerKm()
        );


        workOrder.setComplaint(
                normalizeOptional(
                        request.complaint()
                )
        );


        workOrder.setFindings(
                normalizeOptional(
                        request.findings()
                )
        );


        workOrder.setRecommendations(
                normalizeOptional(
                        request.recommendations()
                )
        );


        workOrder.setStatus(
                request.status()
        );


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // MUNKADÍJ HOZZÁADÁS
    // --------------------------------

    public WorkOrderDto addLaborItem(
            UUID workOrderId,
            CreateLaborItemRequest request
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        LaborItem item =
                new LaborItem();


        item.setDescription(
                request
                        .description()
                        .trim()
        );


        item.setHours(
                request.hours()
        );


        item.setHourlyRate(
                request.hourlyRate()
        );


        workOrder.addLaborItem(
                item
        );


        /*
         * Az új LaborItem kapjon ID-t,
         * mielőtt elkészítjük a választ.
         */
        repository.flush();


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // MUNKADÍJ MÓDOSÍTÁS
    // --------------------------------

    public WorkOrderDto updateLaborItem(
            UUID workOrderId,
            UUID laborItemId,
            UpdateLaborItemRequest request
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        LaborItem item =
                findLaborItem(
                        workOrder,
                        laborItemId
                );


        item.setDescription(
                request
                        .description()
                        .trim()
        );


        item.setHours(
                request.hours()
        );


        item.setHourlyRate(
                request.hourlyRate()
        );


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // MUNKADÍJ TÖRLÉS
    // --------------------------------

    public WorkOrderDto deleteLaborItem(
            UUID workOrderId,
            UUID laborItemId
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        LaborItem item =
                findLaborItem(
                        workOrder,
                        laborItemId
                );


        workOrder.removeLaborItem(
                item
        );


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // ALKATRÉSZ HOZZÁADÁS
    // --------------------------------

    public WorkOrderDto addPartItem(
            UUID workOrderId,
            CreatePartItemRequest request
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        PartItem item =
                new PartItem();


        item.setDescription(
                request
                        .description()
                        .trim()
        );


        item.setSku(
                normalizeOptional(
                        request.sku()
                )
        );


        item.setQuantity(
                request.quantity()
        );


        item.setUnitPrice(
                request.unitPrice()
        );


        workOrder.addPartItem(
                item
        );


        /*
         * FONTOS:
         *
         * A PartItem cascade-del kerül mentésre.
         * A flush kikényszeríti, hogy Hibernate
         * még a DTO elkészítése előtt perzisztálja
         * az új tételt és kiossza az UUID-t.
         */
        repository.flush();


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // ALKATRÉSZ MÓDOSÍTÁS
    // --------------------------------

    public WorkOrderDto updatePartItem(
            UUID workOrderId,
            UUID partItemId,
            UpdatePartItemRequest request
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        PartItem item =
                findPartItem(
                        workOrder,
                        partItemId
                );


        item.setDescription(
                request
                        .description()
                        .trim()
        );


        item.setSku(
                normalizeOptional(
                        request.sku()
                )
        );


        item.setQuantity(
                request.quantity()
        );


        item.setUnitPrice(
                request.unitPrice()
        );


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // ALKATRÉSZ TÖRLÉS
    // --------------------------------

    public WorkOrderDto deletePartItem(
            UUID workOrderId,
            UUID partItemId
    ) {

        WorkOrder workOrder =
                getEditableEntity(
                        workOrderId
                );


        PartItem item =
                findPartItem(
                        workOrder,
                        partItemId
                );


        workOrder.removePartItem(
                item
        );


        return toDto(
                workOrder
        );
    }


    // --------------------------------
    // LEZÁRÁS
    // --------------------------------

    public WorkOrderDto close(
            UUID id
    ) {

        WorkOrder workOrder =
                getEntity(id);


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            return toDto(
                    workOrder
            );
        }


        workOrder.setStatus(
                WorkOrderStatus.COMPLETED
        );


        workOrder.setClosedAt(
                Instant.now()
        );


        return toDto(
                workOrder
        );
    }

    // --------------------------------
    // ENTITY
    // --------------------------------

    public WorkOrder getEntity(
            UUID id
    ) {

        return repository
                .findDetailedById(id)
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "Munkalap nem található: "
                                                + id
                                )
                );
    }


    private WorkOrder getEditableEntity(
            UUID id
    ) {

        WorkOrder workOrder =
                getEntity(id);


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            throw new IllegalStateException(
                    "Lezárt munkalap nem módosítható."
            );
        }


        return workOrder;
    }


    // --------------------------------
    // ITEM KERESÉS
    // --------------------------------

    private LaborItem findLaborItem(
            WorkOrder workOrder,
            UUID itemId
    ) {

        return workOrder
                .getLaborItems()
                .stream()
                .filter(
                        item ->
                                item
                                        .getId()
                                        .equals(itemId)
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "Munkadíj tétel nem található: "
                                                + itemId
                                )
                );
    }


    private PartItem findPartItem(
            WorkOrder workOrder,
            UUID itemId
    ) {

        return workOrder
                .getPartItems()
                .stream()
                .filter(
                        item ->
                                item
                                        .getId()
                                        .equals(itemId)
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "Alkatrész tétel nem található: "
                                                + itemId
                                )
                );
    }


    // --------------------------------
    // DTO
    // --------------------------------

    private WorkOrderDto toDto(
            WorkOrder workOrder
    ) {

        Motorcycle motorcycle =
                workOrder.getMotorcycle();


        List<LaborItemDto> laborItems =
                workOrder
                        .getLaborItems()
                        .stream()
                        .map(
                                this::toLaborDto
                        )
                        .toList();


        List<PartItemDto> partItems =
                workOrder
                        .getPartItems()
                        .stream()
                        .map(
                                this::toPartDto
                        )
                        .toList();


        BigDecimal laborTotal =
                laborItems
                        .stream()
                        .map(
                                LaborItemDto::total
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal partsTotal =
                partItems
                        .stream()
                        .map(
                                PartItemDto::total
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal grandTotal =
                laborTotal.add(
                        partsTotal
                );


        return new WorkOrderDto(

                workOrder.getId(),

                workOrder.getWorkOrderNumber(),

                workOrder.getStatus(),

                workOrder.getOdometerKm(),

                workOrder.getHandedOverOdometerKm(),

                workOrder.getComplaint(),

                workOrder.getFindings(),

                workOrder.getRecommendations(),

                workOrder.getReceivedAt(),

                workOrder.getReadyAt(),

                workOrder.getHandedOverAt(),

                workOrder.getClosedAt(),

                workOrder.getCreatedAt(),

                workOrder.getUpdatedAt(),


                motorcycle.getId(),

                motorcycle.getBrand()
                        + " "
                        + motorcycle.getModel(),

                motorcycle.getLicensePlate(),


                motorcycle
                        .getCustomer()
                        .getId(),

                motorcycle
                        .getCustomer()
                        .getName(),


                laborItems,

                partItems,


                laborTotal,

                partsTotal,

                grandTotal
        );
    }


    private LaborItemDto toLaborDto(
            LaborItem item
    ) {

        return new LaborItemDto(

                item.getId(),

                item.getDescription(),

                item.getHours(),

                item.getHourlyRate(),

                item.calculateTotal()
        );
    }


    private PartItemDto toPartDto(
            PartItem item
    ) {

        return new PartItemDto(

                item.getId(),

                item.getDescription(),

                item.getSku(),

                item.getQuantity(),

                item.getUnitPrice(),

                item.calculateTotal()
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

    public WorkOrderDto handOver(
            UUID id
    ) {

        WorkOrder workOrder =
                getEntity(id);


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            return toDto(
                    workOrder
            );
        }


        Instant now =
                Instant.now();


        if (
                workOrder.getReadyAt() == null
        ) {

            workOrder.setReadyAt(
                    now
            );
        }


        workOrder.setHandedOverAt(
                now
        );


        workOrder.setClosedAt(
                now
        );


        workOrder.setStatus(
                WorkOrderStatus.COMPLETED
        );


        return toDto(
                repository.save(
                        workOrder
                )
        );
    }

    public WorkOrderDto reopen(
            UUID id
    ) {

        WorkOrder workOrder =
                getEntity(id);


        workOrder.setStatus(
                WorkOrderStatus.OPEN
        );


        workOrder.setReadyAt(
                null
        );


        workOrder.setHandedOverAt(
                null
        );


        workOrder.setClosedAt(
                null
        );


        return toDto(
                repository.save(
                        workOrder
                )
        );
    }

    public WorkOrderDto markReady(
            UUID id
    ) {

        WorkOrder workOrder =
                getEntity(id);


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            throw new IllegalStateException(
                    "A lezárt munkalap már nem módosítható."
            );
        }


        workOrder.setStatus(
                WorkOrderStatus.READY_FOR_PICKUP
        );


        if (
                workOrder.getReadyAt() == null
        ) {

            workOrder.setReadyAt(
                    Instant.now()
            );
        }


        return toDto(
                repository.save(
                        workOrder
                )
        );
    }

    public WorkOrderDto handOver(
            UUID id,
            HandOverWorkOrderRequest request
    ) {

        WorkOrder workOrder =
                getEntity(id);


        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            return toDto(
                    workOrder
            );
        }


        if (
                workOrder.getOdometerKm() != null &&
                        request.odometerKm()
                                < workOrder.getOdometerKm()
        ) {

            throw new IllegalArgumentException(
                    "Az átadáskori kilométeróra nem lehet kisebb a beérkezési értéknél."
            );
        }


        Instant now =
                Instant.now();


        if (
                workOrder.getReadyAt() == null
        ) {

            workOrder.setReadyAt(
                    now
            );
        }


        workOrder.setHandedOverOdometerKm(
                request.odometerKm()
        );


        workOrder.setHandedOverAt(
                now
        );


        workOrder.setClosedAt(
                now
        );


        workOrder.setStatus(
                WorkOrderStatus.COMPLETED
        );


        return toDto(
                repository.save(
                        workOrder
                )
        );
    }
}