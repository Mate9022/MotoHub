package hu.motorworkshop.app.photo;

import hu.motorworkshop.app.workorder.WorkOrder;
import hu.motorworkshop.app.workorder.WorkOrderService;
import hu.motorworkshop.app.workorder.WorkOrderStatus;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class WorkOrderPhotoService {

    private static final long MAX_FILE_SIZE =
            15L * 1024L * 1024L;


    private final WorkOrderPhotoRepository repository;

    private final WorkOrderPhotoStorageService storage;

    private final WorkOrderPhotoImageOptimizer imageOptimizer;

    private final WorkOrderService workOrderService;


    public WorkOrderPhotoService(
            WorkOrderPhotoRepository repository,
            WorkOrderPhotoStorageService storage,
            WorkOrderPhotoImageOptimizer imageOptimizer,
            WorkOrderService workOrderService
    ) {

        this.repository =
                repository;

        this.storage =
                storage;

        this.imageOptimizer =
                imageOptimizer;

        this.workOrderService =
                workOrderService;
    }


    // =====================================================
    // LISTA
    // =====================================================

    @Transactional(readOnly = true)
    public List<WorkOrderPhotoDto> list(
            UUID workOrderId
    ) {

        /*
         * Ezzel azt is ellenőrizzük,
         * hogy a munkalap valóban létezik.
         */
        workOrderService.getEntity(
                workOrderId
        );


        return repository
                .findAllByWorkOrder_IdOrderByCreatedAtDesc(
                        workOrderId
                )
                .stream()
                .map(
                        this::toDto
                )
                .toList();
    }


    // =====================================================
    // FELTÖLTÉS
    // =====================================================

    public WorkOrderPhotoDto upload(
            UUID workOrderId,
            MultipartFile file
    ) {

        WorkOrder workOrder =
                workOrderService.getEntity(
                        workOrderId
                );


        ensureEditable(
                workOrder
        );


        validateFile(
                file
        );


        String originalFileName =
                safeOriginalFileName(
                        file.getOriginalFilename()
                );


        try (
                WorkOrderPhotoImageOptimizer.OptimizedPhoto optimizedPhoto =
                        imageOptimizer.optimize(
                                file
                        )
        ) {

            String storedFileName =
                    storage.store(
                            workOrderId,
                            optimizedPhoto.path(),
                            optimizedPhoto.extension()
                    );


            try {

                WorkOrderPhoto photo =
                        new WorkOrderPhoto();


                photo.setWorkOrder(
                        workOrder
                );


                photo.setOriginalFileName(
                        originalFileName
                );


                photo.setStoredFileName(
                        storedFileName
                );


                /*
                 * FONTOS:
                 * már nem az eredeti feltöltés MIME típusa kerül ide,
                 * hanem a ténylegesen eltárolt optimalizált fájlé.
                 */
                photo.setContentType(
                        optimizedPhoto.contentType()
                );


                /*
                 * Szintén az optimalizált fájl tényleges méretét mentjük.
                 */
                photo.setFileSize(
                        optimizedPhoto.fileSize()
                );


                WorkOrderPhoto saved =
                        repository.saveAndFlush(
                                photo
                        );


                return toDto(
                        saved
                );

            } catch (RuntimeException ex) {

                /*
                 * Ha DB mentés közben elszállunk,
                 * ne maradjon árva fájl.
                 */
                try {

                    storage.delete(
                            workOrderId,
                            storedFileName
                    );

                } catch (Exception ignored) {
                }


                throw ex;
            }
        }
    }


    // =====================================================
    // FÁJL BETÖLTÉS
    // =====================================================

    @Transactional(readOnly = true)
    public PhotoContent getContent(
            UUID workOrderId,
            UUID photoId
    ) {

        WorkOrderPhoto photo =
                findPhoto(
                        workOrderId,
                        photoId
                );


        Resource resource =
                storage.load(
                        workOrderId,
                        photo.getStoredFileName()
                );


        return new PhotoContent(
                contentFileName(
                        photo.getOriginalFileName(),
                        photo.getContentType()
                ),
                photo.getContentType(),
                photo.getFileSize(),
                resource
        );
    }


    // =====================================================
    // TÖRLÉS
    // =====================================================

    public void delete(
            UUID workOrderId,
            UUID photoId
    ) {

        WorkOrder workOrder =
                workOrderService.getEntity(
                        workOrderId
                );


        ensureEditable(
                workOrder
        );


        WorkOrderPhoto photo =
                findPhoto(
                        workOrderId,
                        photoId
                );


        repository.delete(
                photo
        );


        repository.flush();


        storage.delete(
                workOrderId,
                photo.getStoredFileName()
        );
    }


    // =====================================================
    // SEGÉDEK
    // =====================================================

    private WorkOrderPhoto findPhoto(
            UUID workOrderId,
            UUID photoId
    ) {

        return repository
                .findByIdAndWorkOrder_Id(
                        photoId,
                        workOrderId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        NOT_FOUND,
                                        "A fotó nem található."
                                )
                );
    }


    private void ensureEditable(
            WorkOrder workOrder
    ) {

        if (
                workOrder.getStatus()
                        == WorkOrderStatus.COMPLETED
        ) {

            throw new IllegalStateException(
                    "Lezárt munkalap fotói nem módosíthatók."
            );
        }
    }


    private void validateFile(
            MultipartFile file
    ) {

        if (
                file == null ||
                        file.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "A feltöltött fájl üres."
            );
        }


        if (
                file.getSize()
                        > MAX_FILE_SIZE
        ) {

            throw new IllegalArgumentException(
                    "A kép legfeljebb 15 MB lehet."
            );
        }

        /*
         * Szándékosan NEM a file.getContentType() alapján döntünk.
         * A MIME típust a kliens küldi, ezért nem megbízható.
         * A WorkOrderPhotoImageOptimizer ImageIO-val a tényleges
         * fájlformátumot ellenőrzi.
         */
    }


    private String safeOriginalFileName(
            String originalFileName
    ) {

        if (
                originalFileName == null ||
                        originalFileName.isBlank()
        ) {

            return "photo";
        }


        String normalized =
                originalFileName
                        .replace(
                                "\\",
                                "/"
                        );


        int slash =
                normalized
                        .lastIndexOf('/');


        String fileName =
                slash >= 0
                        ? normalized.substring(
                        slash + 1
                )
                        : normalized;


        if (
                fileName.length() > 500
        ) {

            return fileName.substring(
                    fileName.length() - 500
            );
        }


        return fileName;
    }


    private String contentFileName(
            String originalFileName,
            String contentType
    ) {

        if (
                !"image/jpeg".equalsIgnoreCase(
                        contentType
                )
        ) {

            return originalFileName;
        }


        int dot =
                originalFileName.lastIndexOf('.');


        String baseName =
                dot > 0
                        ? originalFileName.substring(
                        0,
                        dot
                )
                        : originalFileName;


        if (baseName.isBlank()) {

            baseName =
                    "photo";
        }


        return baseName + ".jpg";
    }


    private WorkOrderPhotoDto toDto(
            WorkOrderPhoto photo
    ) {

        return new WorkOrderPhotoDto(

                photo.getId(),

                photo.getOriginalFileName(),

                photo.getContentType(),

                photo.getFileSize(),

                photo.getCreatedAt()
        );
    }


    public record PhotoContent(

            String fileName,

            String contentType,

            Long fileSize,

            Resource resource

    ) {
    }
}
