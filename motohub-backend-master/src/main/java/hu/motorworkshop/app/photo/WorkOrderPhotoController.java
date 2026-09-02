package hu.motorworkshop.app.photo;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/work-orders/{workOrderId}/photos"
)
public class WorkOrderPhotoController {

    private final WorkOrderPhotoService service;


    public WorkOrderPhotoController(
            WorkOrderPhotoService service
    ) {

        this.service =
                service;
    }


    // =====================================================
    // LISTA
    // =====================================================

    @GetMapping
    public List<WorkOrderPhotoDto> list(
            @PathVariable UUID workOrderId
    ) {

        return service.list(
                workOrderId
        );
    }


    // =====================================================
    // FELTÖLTÉS
    // =====================================================

    @PostMapping(
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public WorkOrderPhotoDto upload(

            @PathVariable
            UUID workOrderId,

            @RequestParam("file")
            MultipartFile file

    ) {

        return service.upload(
                workOrderId,
                file
        );
    }


    // =====================================================
    // KÉP
    // =====================================================

    @GetMapping(
            "/{photoId}/content"
    )
    public ResponseEntity<Resource> content(

            @PathVariable
            UUID workOrderId,

            @PathVariable
            UUID photoId

    ) {

        WorkOrderPhotoService.PhotoContent photo =
                service.getContent(
                        workOrderId,
                        photoId
                );


        MediaType mediaType;


        try {

            mediaType =
                    MediaType.parseMediaType(
                            photo.contentType()
                    );

        } catch (Exception ex) {

            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }


        ContentDisposition disposition =
                ContentDisposition
                        .inline()
                        .filename(
                                photo.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();


        return ResponseEntity
                .ok()
                .contentType(
                        mediaType
                )
                .contentLength(
                        photo.fileSize()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(
                        photo.resource()
                );
    }


    // =====================================================
    // TÖRLÉS
    // =====================================================

    @DeleteMapping(
            "/{photoId}"
    )
    @ResponseStatus(
            HttpStatus.NO_CONTENT
    )
    public void delete(

            @PathVariable
            UUID workOrderId,

            @PathVariable
            UUID photoId

    ) {

        service.delete(
                workOrderId,
                photoId
        );
    }
}